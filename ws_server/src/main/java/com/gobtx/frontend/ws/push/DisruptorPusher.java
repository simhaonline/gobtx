package com.gobtx.frontend.ws.push;

import com.gobtx.frontend.ws.hub.MarketHubClientListener;
import com.gobtx.frontend.ws.json.JSonHelper;
import com.gobtx.frontend.ws.netty.WebSocketSubscribeContext;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import com.gobtx.model.view.TradeEventWithExchangeView;
import com.google.gson.Gson;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Created by Aaron Kuai on 2019/11/20. */
@Component
@Profile("disruptor-pusher")
public class DisruptorPusher implements MarketHubClientListener {

  final Logger logger = LoggerFactory.getLogger(DisruptorPusher.class);

  final AtomicBoolean started = new AtomicBoolean(false);

  @Autowired public DisruptorProperties properties;

  @Autowired
  @Qualifier("globalGson")
  public Gson gson;

  protected Disruptor<PushEvent> disruptor;

  protected int parallelCnt;

  private volatile boolean notStart = true;

  private RingBuffer<PushEvent> ringBuffer;

  @Override
  public void start() {
    if (started.compareAndSet(false, true)) {

      disruptor =
          new Disruptor<>(
              PushEvent::new,
              properties.getBufferSize(),
              new ThreadFactory() {

                final AtomicLong cnt = new AtomicLong();

                @Override
                public Thread newThread(Runnable r) {

                  Thread res = new Thread(r, "DIS-PUSHER-" + cnt.incrementAndGet());

                  res.setUncaughtExceptionHandler((t, e) -> logger.error("FAIL_PUSHER {}", e));
                  return res;
                }
              },
              properties.getProducerType(),
              new BlockingWaitStrategy());

      disruptor.setDefaultExceptionHandler(new ExceptionHandler());

      parallelCnt = properties.getCpuModel().count;

      PushEventHandler[] handlers = new PushEventHandler[parallelCnt];

      for (int i = 0; i < parallelCnt; i++) {
        handlers[i] = new PushEventHandler(i);
      }

      disruptor.handleEventsWith(handlers);
      // How many handler?
      disruptor.start();
      notStart = false;
      ringBuffer = disruptor.getRingBuffer();
    }
  }

  @Override
  public void stop() {
    notStart = true;
    if (started.compareAndSet(true, false)) {

      long lastChangeDetected = System.currentTimeMillis();
      long lastKnownCursor = disruptor.getRingBuffer().getCursor();

      while (System.currentTimeMillis() - lastChangeDetected < 5_000 && !Thread.interrupted()) {

        logger.warn("next round wait the disruptor Done");

        if (disruptor.getRingBuffer().getCursor() != lastKnownCursor) {
          lastChangeDetected = System.currentTimeMillis();
          lastKnownCursor = disruptor.getRingBuffer().getCursor();
        }
      }
    }
  }

  final AtomicLong randomSequence = new AtomicLong(1);

  final WebSocketSubscribeContext WSS_CTX = WebSocketSubscribeContext.getInstance();

  @Override
  public void handleTradeEvent(TradeEventWithExchangeView data) {

    if (notStart) {
      logger.warn("NOT_START_YET_IGNORE_TRADE_EVT");
      return;
    }

    final Collection<ChannelHandlerContext> targets =
        WSS_CTX.tickClients(data.getExchange(), data.getSymbol());

    logger.debug("TRADE_EVENT {},{}", data.getExchange(), data.getSymbol());

    if (targets.isEmpty()) {

      // if (logger.isDebugEnabled()) {
      //  logger.debug("NO_CLIENT_TRADE_EVT_IGNORE {},{}", data.getExchange(), data.getSymbol());
      // }

      if (logger.isDebugEnabled() && data.getExchange() == Exchange.HUOBI) {
        logger.debug("NO_CLIENT_TRADE_EVT_IGNORE {},{}", data.getExchange(), data.getSymbol());
      }

      return;
    }

    final ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(128);

    // final String payload = gson.toJson(data);
    // buf.writeCharSequence(payload, StandardCharsets.UTF_8);
    JSonHelper.encodeTradeEvent(data, buf);

    push2Ringbuffer(buf, targets);
  }

  private void push2Ringbuffer(final ByteBuf buf, final Collection<ChannelHandlerContext> targets) {

    for (ChannelHandlerContext ctx : targets) {

      if (ctx.channel().isWritable()) {
        final long sequence = ringBuffer.next();
        try {

          final PushEvent next = ringBuffer.get(sequence);

          final int segmentId = (int) ((randomSequence.incrementAndGet()) % parallelCnt);

          next.reset(segmentId, ctx.channel(), buf);

        } finally {
          ringBuffer.publish(sequence);
        }
      } else {
        logger.warn("NO_WRITABLE_CHANNEL_IGNORE {}", ctx.channel());
      }
    }
  }

  @Override
  public void handle(final OHLCWithExchangeAndIntervalView data) {

    if (notStart) {
      logger.warn("NOT_START_YET_IGNORE_MKT_DATA");
      return;
    }

    // Some delay no usage
    if (data.getOpenTime() < System.currentTimeMillis() - 10_000) {
      return;
    }

    // Try to find the target customers

    final Collection<ChannelHandlerContext> targets =
        WSS_CTX.klineClients(data.getExchange(), data.getInterval(), data.getSymbol());

    if (targets.isEmpty()) {

      //      if (logger.isDebugEnabled()) {
      //        logger.debug(
      //            "NO_CLIENT_MKT_IGNORE {},{},{},{}",
      //            data.getExchange(),
      //            data.getInterval(),
      //            data.getSymbol(),
      //            data.getTimeKey());
      //      }
      return;
    }

    final ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(128);

    // final String payload = gson.toJson(data);
    // buf.writeCharSequence(payload, StandardCharsets.UTF_8);
    JSonHelper.encodeMarketData(data, buf);

    push2Ringbuffer(buf, targets);
  }

  private class ExceptionHandler implements com.lmax.disruptor.ExceptionHandler {

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
      logger.error("Exception occurred while processing a {}.", event, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
      logger.error("Failed to start the DisruptorPriceAggregatePublisher.", ex);
      disruptor.shutdown();
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
      logger.error("Error while shutting down the DisruptorPriceAggregatePublisher", ex);
    }
  }
}
