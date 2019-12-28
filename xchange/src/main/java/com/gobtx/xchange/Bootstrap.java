package com.gobtx.xchange;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.gobtx.common.Env;
import com.gobtx.common.Utils;
import com.gobtx.common.executor.GlobalExecutorService;
import com.gobtx.hub.application.AcceptorContextManager;
import com.gobtx.hub.application.AcceptorHubApplication;
import com.gobtx.hub.bootstrap.HubServerBootstrap;
import com.gobtx.hub.bootstrap.Status;
import com.gobtx.hub.netty.HubChannelInitiator;
import com.gobtx.hub.netty.HubMarketDataMessageDecoder;
import com.gobtx.hub.netty.HubMarketDataMessageEncoder;
import com.gobtx.hub.protocol.CodecHelper;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.enums.KlineIntervalGroup;
import com.gobtx.model.view.OHLCView;
import com.gobtx.model.view.TradeEventView;
import com.gobtx.xchange.aggregator.MarketStreamAggregator;
import com.gobtx.xchange.aggregator.PostAggregateListener;
import com.gobtx.xchange.configuration.properties.ServerProperties;
import com.gobtx.xchange.service.BaseService;
import com.gobtx.xchange.service.MarketDataService;
import com.gobtx.xchange.service.meta.MetaDataService;
import com.google.common.base.Predicates;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStoppedEvent;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.gobtx.common.Constants.MKT_DATA;
import static com.gobtx.common.Constants.TRADE_DATA;
import static com.gobtx.xchange.configuration.ExchangeIntervalMapper.registerIntervalGroup;

/** Created by Aaron Kuai on 2019/11/13. */
@Configuration
@SuppressWarnings("Duplicates")
public class Bootstrap implements SmartLifecycle, ApplicationListener {

  static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

  @Autowired MarketStreamAggregator aggregator;

  @Autowired(required = false)
  List<MarketDataService> marketDataServices;

  @Autowired(required = false)
  List<MetaDataService> metaDataServices;

  final AtomicBoolean started = new AtomicBoolean(false);

  @Override
  public void start() {
    if (started.compareAndSet(false, true)) {
      logger.warn("START_BOOTSTRAP");
    }
  }

  @Override
  public void stop() {
    if (started.compareAndSet(true, false)) {
      logger.warn("STOP_BOOTSTRAP");
    }
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public boolean isRunning() {
    return started.get();
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    // ContextRefreshedEvent
    // ApplicationStartedEvent
    // ApplicationReadyEvent
    logger.warn("BOOTSTRAP_APPLICATION_EVENT {}", event.getClass().getName());
    if (event instanceof ApplicationReadyEvent) {
      doStart();
    } else if (event instanceof ContextStoppedEvent) {
      doStop();
    }
  }

  private void doStop() {

    marketDataServices.forEach(
        it -> {
          it.stop();
        });

    if (rpcServerBootstrap != null) {
      rpcServerBootstrap.stop();
    }
  }

  @Value("${enable.exchanges}")
  public String enableExchanges;

  final AcceptorContextManager acm = AcceptorContextManager.getInstance();

  public TradeDataStreamListener tradeListener =
      new TradeDataStreamListener() {
        @Override
        public void update(TradeEventView data, Exchange exchange, String symbol) {

          if (acm.isEmpty()) {
            logger.warn("NO_DOWN_STREAM_LISTEN_TRADE_DATA {},{}", exchange, symbol);
            return;
          }

          if (logger.isDebugEnabled()) {
            logger.debug("PUB_TRADE_EVENT_SINGLE {},{}", exchange, symbol);
          }

          final ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(128);

          buffer.writeByte(TRADE_DATA);
          buffer.writeByte(exchange.ordinal());

          CodecHelper.encodeTradeEvent(buffer, data);

          try {
            for (final ChannelHandlerContext ctx : acm.getRegistered().values()) {

              if (ctx.channel().isWritable()) {
                ctx.writeAndFlush(buffer.retainedDuplicate(), ctx.voidPromise());
              }
            }
          } finally {
            ReferenceCountUtil.release(buffer);
          }
        }

        @Override
        public void updates(List<TradeEventView> dataList, Exchange exchange, String symbol) {

          if (acm.isEmpty()) {
            logger.warn(
                "NO_DOWN_STREAM_LISTEN_TRADE_DATA_LIST {},{},[{}]",
                exchange,
                symbol,
                dataList.size());
            return;
          }

          if (logger.isDebugEnabled()) {
            logger.debug("PUB_TRADE_EVENT_LIST {},{},[{}]", exchange, symbol, dataList.size());
          }

          final ByteBuf[] buffers = new ByteBuf[dataList.size()];
          int size = dataList.size();
          for (int i = 0; i < size; i++) {
            buffers[i] = PooledByteBufAllocator.DEFAULT.directBuffer(128);
            buffers[i].writeByte(TRADE_DATA);
            buffers[i].writeByte(exchange.ordinal()); // (exchange.ordinal());
            CodecHelper.encodeTradeEvent(buffers[i], dataList.get(i));
          }

          try {
            for (final ChannelHandlerContext ctx : acm.getRegistered().values()) {

              if (ctx.channel().isWritable()) {
                for (ByteBuf buffer : buffers)
                  ctx.write(buffer.retainedDuplicate(), ctx.voidPromise());
              }
              ctx.flush();
            }
          } finally {
            for (int i = 0; i < size; i++) {
              ReferenceCountUtil.release(buffers[i]);
            }
          }
        }
      };

  private void doStart() {

    logger.warn("BOOTSTRAP_SPRING_CONTEXT_READY {}", enableExchanges);
    // Try to wire them together

    final Set<Exchange> supportExchange = new HashSet<>();

    for (final String ex : enableExchanges.split(",")) {

      final Exchange exchange = Exchange.valueOf(ex.toUpperCase());
      supportExchange.add(exchange);
    }

    if (marketDataServices == null || marketDataServices.isEmpty()) {

      logger.error("NO_MARKET_DATA_SERVICE");
      System.exit(1);
    }

    // From very begin try to start the aggregator
    aggregator.start();

    // Trigger all the server's things

    // Hook the listener to the

    final List<MarketDataService> supported =
        marketDataServices.stream()
            .filter(it -> supportExchange.contains(it.exchange()))
            .collect(Collectors.toList());

    if (supported.isEmpty()) {
      logger.error("NO_MARKET_DATA_SERVICE_SUPPORT");
      System.exit(1);
    }

    final CountDownLatch countDownLatch = new CountDownLatch(supported.size());

    LoadMode.init();
    logger.warn("INIT_LOAD_MODE {}", LoadMode.MODE);

    supported.forEach(
        it -> {
          GlobalExecutorService.INSTANCE.submit(
              () -> {

                // Register the support mapper
                final List<KlineIntervalGroup> groups =
                    KlineIntervalGroup.group(it.supportKlineIntervals());

                registerIntervalGroup(it.exchange(), groups);

                logger.warn("TRY_INIT_CLIENT {},{}", it.exchange(), it.version());
                it.listener(aggregator);
                it.tradeListener(tradeListener);
                it.start();
                countDownLatch.countDown();
              });
        });

    try {
      countDownLatch.await();

      logger.warn(
          "EVENTUALLY_ALL_EXCHANGES_DONE {}",
          supported.stream().map(BaseService::exchange).collect(Collectors.toList()));

    } catch (InterruptedException e) {
      logger.error("FAIL_START_ALL_SERVER {}", e);
      System.exit(1);
    }
    // At last schedule regular refresh of the batch information
    // Start host let downstream to catch the ghing

    startHubServer();
  }

  @Value("#{systemProperties['LOCAL_ADDRESS'] ?: ''}")
  public String localAddress;

  @Autowired public ServerProperties rpcProperties;

  protected EventLoopGroup bossEventLoop;

  protected EventLoopGroup workerEventloop;

  HubServerBootstrap rpcServerBootstrap;

  private void startHubServer() {
    // Then hook to the aggregator listener

    // aggregator.postAggregateListener()

    logger.warn(
        "EVENT_LOOP_INIT LINUX:{},SHARE:{}, {},{}",
        Utils.IS_OS_LINUX,
        rpcProperties.isShareEventLoop(),
        rpcProperties.getCpuModel().count,
        rpcProperties.isShareEventLoop()
            ? rpcProperties.getCpuModel().count
            : rpcProperties.getWorkCpuModel().count);

    if (Utils.IS_OS_LINUX) {

      bossEventLoop = new EpollEventLoopGroup(rpcProperties.getCpuModel().count);
      if (rpcProperties.isShareEventLoop()) {
        workerEventloop = bossEventLoop;
      } else {
        workerEventloop = new EpollEventLoopGroup(rpcProperties.getWorkCpuModel().count);
      }
    } else {
      bossEventLoop = new NioEventLoopGroup(rpcProperties.getCpuModel().count);
      if (rpcProperties.isShareEventLoop()) {
        workerEventloop = bossEventLoop;
      } else {
        workerEventloop = new NioEventLoopGroup(rpcProperties.getCpuModel().count);
      }
    }

    InetSocketAddress address;

    if (Env.isProd()) {
      logger.warn("PROD_ENV_USER_INNER_IP {}", localAddress);
      address = new InetSocketAddress(localAddress, rpcProperties.getPort());
    } else {
      address = new InetSocketAddress(rpcProperties.getPort());
    }

    logger.warn("RPC_SERVER_HOST_ONE {}", address);

    HubChannelInitiator hubChannelInitiator =
        new HubChannelInitiator(
            rpcProperties.isServerDebug(),
            new HubMarketDataMessageDecoder(),
            new HubMarketDataMessageEncoder(),
            new AcceptorHubApplication(AcceptorContextManager.getInstance()) {
              @Override
              protected void handleReadIdle(ChannelHandlerContext ctx) {
                logger.warn("CLIENT_READ_IDLE {}", ctx);
              }

              @Override
              protected void handleWriteIdle(ChannelHandlerContext ctx) {
                logger.warn("CLIENT_WRITE_IDLE {}", ctx);
              }

              @Override
              protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

                logger.warn("EXCEPT_PING_PONG_SUPPOSE_HAS_NOT_DATA {}", msg);
              }
            });

    final Retryer<Boolean> retryer =
        RetryerBuilder.<Boolean>newBuilder()
            .retryIfResult(Predicates.<Boolean>isNull())
            .retryIfRuntimeException()
            .withStopStrategy(StopStrategies.neverStop())
            .withWaitStrategy(WaitStrategies.fixedWait(8, TimeUnit.SECONDS))
            .build();

    final Callable<Boolean> callable =
        () -> {
          final CountDownLatch countDownLatch = new CountDownLatch(1);

          HubServerBootstrap bootstrap =
              new HubServerBootstrap(
                  bossEventLoop,
                  workerEventloop,
                  address,
                  hubChannelInitiator,
                  (bootStrap, status, args) -> {
                    logger.warn("RPC_EVENT {},{}", status, address);
                    if (Status.READY == status) {
                      countDownLatch.countDown();
                    }
                  });

          bootstrap.start();
          rpcServerBootstrap = bootstrap;
          try {
            countDownLatch.await(5, TimeUnit.SECONDS);
          } catch (Throwable throwable) {
            logger.error("FAIL_START_RPC_SERVER_IN_5M {}", throwable);
            return false;
          }
          return true;
        };

    try {
      retryer.call(callable);
    } catch (Throwable e) {
      logger.error("FAIL_START_RPC_SERVER_WITH_RETRY {}", e);
      shutdownInstance();
    }
    // success then hook the listener

    // TODO: 2019/11/21  manually switch it off
    aggregator.postAggregateListener(
        new PostAggregateListener() {
          @Override
          public void one(
              Exchange exchange,
              String symbol,
              KlineInterval interval,
              final OHLCView data,
              final boolean derived) {

            if (!derived) {
              // if (logger.isDebugEnabled()) {
              //  logger.debug("NO_DERIVED_SO_IGNORE {},{},{}", exchange, symbol, interval);
              // }
              return;
            }

            if (acm.isEmpty() && logger.isDebugEnabled()) {
              logger.debug("NO_DOWN_STREAM_LISTEN_SINGLE {},{}", exchange, symbol);
              return;
            }

            final ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(128);

            buffer.writeByte(MKT_DATA);
            buffer.writeByte(exchange.ordinal());
            buffer.writeByte(interval.ordinal());
            CodecHelper.encodeTradeEvent(buffer, data);

            try {
              for (final ChannelHandlerContext ctx : acm.getRegistered().values()) {

                if (ctx.channel().isWritable()) {
                  ctx.writeAndFlush(buffer.retainedDuplicate(), ctx.voidPromise());
                }
              }
            } finally {
              ReferenceCountUtil.release(buffer);
            }
          }

          @Override
          public void batch(
              Exchange exchange,
              String symbol,
              KlineInterval[] intervals,
              OHLCView[] dataList,
              final boolean derived) {

            if (!derived) {
              // if (logger.isDebugEnabled()) {
              //  logger.debug("NO_DERIVED_SO_IGNORE_BATCH {},{}", exchange, symbol);
              // }
              return;
            }
            if (acm.isEmpty() && logger.isDebugEnabled()) {
              logger.debug("NO_DOWN_STREAM_LISTEN_BATCH {},{}", exchange, symbol);
              return;
            }

            final ByteBuf[] bbs = new ByteBuf[intervals.length];
            final int len = intervals.length;

            for (int i = 0; i < len; i++) {

              bbs[i] = PooledByteBufAllocator.DEFAULT.directBuffer(128);
              bbs[i].writeByte(MKT_DATA);
              bbs[i].writeByte(exchange.ordinal());
              bbs[i].writeByte(intervals[i].ordinal());
              CodecHelper.encodeTradeEvent(bbs[i], dataList[i]);
            }

            try {
              for (final ChannelHandlerContext ctx : acm.getRegistered().values()) {

                if (ctx.channel().isWritable()) {

                  for (final ByteBuf bb : bbs) {
                    ctx.write(bb.retainedDuplicate(), ctx.voidPromise());
                  }
                  ctx.flush();
                }
              }
            } finally {
              for (int i = 0; i < len; i++) {

                ReferenceCountUtil.release(bbs[i]);
              }
            }
          }
        });
  }

  @Autowired ApplicationContext applicationContext;

  protected void shutdownInstance() {

    if (applicationContext != null) {
      if (applicationContext instanceof ConfigurableApplicationContext) {
        ((ConfigurableApplicationContext) applicationContext).close();
      } else {
        logger.error("FORCE_KILL_SELF1");
        System.exit(1);
      }
    } else {
      logger.error("FORCE_KILL_SELF2");
      System.exit(2);
    }
  }
}
