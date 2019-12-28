package com.gobtx.xchange.disruptor;

import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import com.gobtx.xchange.aggregator.MarketStreamAggregator;
import com.gobtx.xchange.aggregator.PostAggregateListener;
import com.gobtx.xchange.function.ExchangeCalenderFunctionFactory;
import com.gobtx.xchange.repository.MarketDataRepository;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Created by Aaron Kuai on 2019/11/11. */
public class DisruptorMarketStreamAggregator implements MarketStreamAggregator {

  static final Logger logger = LoggerFactory.getLogger(DisruptorMarketStreamAggregator.class);

  // Should we partition those symbol and speed up all those thing?

  protected CopyOnWriteArrayList<PostAggregateListener> listeners = new CopyOnWriteArrayList<>();

  private volatile boolean notStart = false;

  protected final AtomicBoolean started = new AtomicBoolean(false);

  protected Disruptor<MarketEvent> disruptor;

  protected final MarketDataRepository repository;

  protected final boolean disruptorFlush;

  protected final int processParallelCnt;

  protected final int delayInSeconds;

  ScheduledFuture scheduledFuture;

  protected ExchangeCalenderFunctionFactory calenderFactory =
      ExchangeCalenderFunctionFactory.getInstance();

  public DisruptorMarketStreamAggregator(
      final DisruptorConfiguration configuration, final MarketDataRepository repository) {

    processParallelCnt = configuration.getProcessParallelCnt();
    delayInSeconds = configuration.getDelayInSeconds();

    disruptor =
        new Disruptor<>(
            () -> new MarketEvent(),
            configuration.getBufferSize(),
            new ThreadFactory() {

              final AtomicLong cnt = new AtomicLong();

              @Override
              public Thread newThread(Runnable r) {

                Thread res = new Thread(r, "DIS-MKT-AGG-" + cnt.incrementAndGet());

                res.setUncaughtExceptionHandler(
                    (t, e) -> logger.error("FAIL_HANDLE_MARKET_DATA_AGG_EXCEPTION {}", e));
                return res;
              }
            },
            configuration.getProducerType(),
            configuration.getWaitStrategy());
    this.repository = repository;

    disruptor.setDefaultExceptionHandler(new ExceptionHandler());

    MarketEventHandler[] eventHandlers =
        new MarketEventHandler
            [configuration.processParallelCnt <= 0 ? 1 : configuration.processParallelCnt];

    for (int i = 0; i < configuration.processParallelCnt; i++) {
      eventHandlers[i] = new MarketEventHandler(repository, i, listeners);
    }

    disruptorFlush = configuration.isDisruptorFlush();

    disruptor.handleEventsWith(eventHandlers);

    // Add handler

  }

  @Override
  public void update(
      final OHLCView data,
      final KlineInterval interval,
      final Exchange exchange,
      final String symbol,
      final boolean derived) {

    if (notStart) {
      logger.error("DISRUPTOR_NOT_START_YET {},{},{}", exchange, symbol, interval);
      return;
    } else {

      // if (logger.isDebugEnabled()) {
      //  logger.debug("DIS_AGG {},{},{}", exchange, symbol, interval);
      // }

      final RingBuffer<MarketEvent> ringBuffer = disruptor.getRingBuffer();

      long sequence = ringBuffer.next();
      try {

        final MarketEvent next = ringBuffer.get(sequence);

        final int segmentId = (symbol.hashCode() & Integer.MAX_VALUE) % processParallelCnt;

        next.reset(
            segmentId,
            data,
            interval,
            exchange,
            symbol,
            calenderFactory.function(exchange).calenderFromKey(data.getOpenTime(), symbol),
            derived);

      } finally {
        ringBuffer.publish(sequence);
      }
    }
  }

  @Override
  public void start() {
    if (started.compareAndSet(false, true)) {

      logger.warn("DISRUPTOR_START");

      disruptor.start();

      if (disruptorFlush) {
        // TODO enable the schedule trigger

        logger.warn("DISRUPTOR_FLUSH_ENABLE {}s", delayInSeconds);
        scheduledFuture =
            GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
                () -> {
                  if (!notStart) {

                    final RingBuffer<MarketEvent> ringBuffer = disruptor.getRingBuffer();

                    long sequence = ringBuffer.next();
                    try {

                      MarketEvent next = ringBuffer.get(sequence);
                      next.regular();

                      if (logger.isDebugEnabled()) {
                        logger.debug("TRIGGER_REGULAR_FLUSH_JOB_OF_DISRUPTOR");
                      }
                    } finally {
                      ringBuffer.publish(sequence);
                    }
                  }
                },
                delayInSeconds,
                delayInSeconds,
                TimeUnit.SECONDS);
      }
      notStart = false;
    }
  }

  @Override
  public void stop() {

    notStart = true;

    if (started.compareAndSet(true, false)) {

      if (scheduledFuture != null) {

        try {
          scheduledFuture.cancel(true);
        } catch (Throwable throwable) {

        }
      }

      final RingBuffer<MarketEvent> ringBuffer = disruptor.getRingBuffer();

      long sequence = ringBuffer.next();
      try {

        MarketEvent next = ringBuffer.get(sequence);
        next.posion();
      } finally {
        ringBuffer.publish(sequence);
      }

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

  @Override
  public Closeable postAggregateListener(final PostAggregateListener listener) {

    listeners.add(listener);
    return new Closeable() {
      @Override
      public void close() {
        listeners.remove(listener);
      }
    };
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
