package com.gobtx.frontend.ws.config;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.gobtx.common.Env;
import com.gobtx.common.Utils;
import com.gobtx.frontend.ws.hub.HubBootstrap;
import com.gobtx.frontend.ws.hub.MarketHubClientListener;
import com.gobtx.frontend.ws.netty.WebSocketChannelInitializer;
import com.google.common.base.Predicates;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.gobtx.common.Utils.IS_OS_LINUX;

/**
 * Created by Aaron Kuai on 2019/11/18.
 *
 * <p>This is the stand alone
 */
@Component
@SuppressWarnings("Duplicates")
public class WebSocketBootstrap implements ApplicationListener {

  static final Logger logger = LoggerFactory.getLogger(WebSocketBootstrap.class);

  @Autowired WebSocketProperties webSocketProperties;

  @Value("#{systemProperties['LOCAL_ADDRESS'] ?: ''}")
  public String localAddress;

  @Autowired ApplicationContext applicationContext;

  @Override
  public void onApplicationEvent(ApplicationEvent event) {

    // ReactiveWebServerInitializedEvent
    // ApplicationStartedEvent
    // ApplicationReadyEvent

    logger.warn("BOOTSTRAP_APPLICATION_EVENT {}", event.getClass().getName());
    if (event instanceof ApplicationReadyEvent) {
      try {
        doStart();
      } catch (InterruptedException e) {
        logger.error("FAIL_START_INSTANCE  {}", e);
        shutdownInstance();
      }
    } else if (event instanceof ContextStoppedEvent) {
      doStop();
    }
  }

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

  protected EventLoopGroup bossEventLoop;

  protected EventLoopGroup workerEventloop;

  private void doStart() throws InterruptedException {

    logger.warn(
        "EVENT_LOOP_INIT LINUX:{},SHARE:{}, {},{}",
        Utils.IS_OS_LINUX,
        webSocketProperties.isShareEventLoop(),
        webSocketProperties.getCpuModel().count,
        webSocketProperties.shareEventLoop
            ? webSocketProperties.getCpuModel().count
            : webSocketProperties.getWorkCpuModel().count);

    if (Utils.IS_OS_LINUX) {

      bossEventLoop = new EpollEventLoopGroup(webSocketProperties.getCpuModel().count);
      if (webSocketProperties.shareEventLoop) {
        workerEventloop = bossEventLoop;
      } else {
        workerEventloop = new EpollEventLoopGroup(webSocketProperties.getWorkCpuModel().count);
      }
    } else {
      bossEventLoop = new NioEventLoopGroup(webSocketProperties.getCpuModel().count);
      if (webSocketProperties.shareEventLoop) {
        workerEventloop = bossEventLoop;
      } else {
        workerEventloop = new NioEventLoopGroup(webSocketProperties.getCpuModel().count);
      }
    }

    // 1. Start the HUB  bootstrap client
    // 2. Start the Web Socket Server

    startWebSocket();

    startMarketClientHubs();
  }

  private Channel rpcServerChannel;

  @Autowired
  @Qualifier("globalGson")
  public Gson gson;

  private void startWebSocket() {

    final ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap
        .group(bossEventLoop, workerEventloop)
        .channel(IS_OS_LINUX ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
        .childHandler(new WebSocketChannelInitializer(webSocketProperties.isServerDebug(), gson));

    InetSocketAddress address;

    if (Env.isProd()) {
      logger.warn("PROD_ENV_USER_INNER_IP {}", localAddress);
      address = new InetSocketAddress(localAddress, webSocketProperties.getPort());
    } else {
      address = new InetSocketAddress(webSocketProperties.getPort());
    }

    bootstrap.localAddress(address);
    bootstrap.validate();

    // Before is the kick off logic try use the retry things

    final Retryer<Boolean> retryer =
        RetryerBuilder.<Boolean>newBuilder()
            .retryIfResult(Predicates.<Boolean>isNull())
            .retryIfRuntimeException()
            .withStopStrategy(StopStrategies.neverStop())
            .withWaitStrategy(WaitStrategies.fixedWait(8, TimeUnit.SECONDS))
            .build();

    final AtomicLong retryCnt = new AtomicLong(0);

    final Callable<Boolean> callable =
        () -> {
          final CountDownLatch countDownLatch = new CountDownLatch(1);
          final ChannelFuture channelFuture = bootstrap.bind().sync();

          logger.warn("PREPARE_KICK_OFF_WEB_SOCKET ROUND: {}", retryCnt.incrementAndGet());

          channelFuture.addListener(
              future -> {
                if (future.isSuccess()) {
                  rpcServerChannel = channelFuture.channel();
                  logger.warn("WEBSOCKET_SERVER_READY {}", rpcServerChannel.localAddress());

                  rpcServerChannel
                      .closeFuture()
                      .addListener(
                          future1 -> {
                            logger.warn("WEBSOCKET_CHANEL_SHUT_DOWN {}", future1.isSuccess());
                            // TODO: 2019/11/19  other purge stuff
                          });
                  countDownLatch.countDown();
                } else {
                  logger.error("FAIL_KICK_OFF_WEB_SOCKET_SERVER {}", future.cause());
                  // TODO Re-try?
                }
              });

          countDownLatch.await(10, TimeUnit.SECONDS);

          return true;
        };

    try {
      retryer.call(callable);
    } catch (final Exception e) {
      logger.error("FAIL_START_WEB_SOCKET_SERVER {}", e);
      shutdownInstance();
    }
  }

  @Autowired(required = false)
  HubBootstrap marketHubClientBootstrap;

  @Autowired(required = false)
  MarketHubClientListener marketHubClientListener;

  private void startMarketClientHubs() {

    // if in mock env

    if (marketHubClientListener != null) {
      marketHubClientListener.start();
    }

    if (marketHubClientBootstrap != null) {
      marketHubClientBootstrap.start(workerEventloop);
    } else {
      logger.warn("NO_RPC_HUB_SO_MOCK_HUB_CLIENT {}", Env.current());
    }
  }

  private void doStop() {

    logger.warn("[BIZ] STOP_SOCKET_SERVER");
    try {
      if (rpcServerChannel != null) {
        rpcServerChannel.close().sync();
        rpcServerChannel = null;
      }

      if (marketHubClientBootstrap != null) {
        marketHubClientBootstrap.stop();
      }

      if (marketHubClientListener != null) {
        marketHubClientListener.stop();
      }

    } catch (InterruptedException e) {
      logger.error("FAIL_SHUT_DOWN_WEB_SOCKET_SERVER", e);
    } finally {

      bossEventLoop.shutdownGracefully();
      bossEventLoop = null;
      workerEventloop.shutdownGracefully();
      workerEventloop = null;
    }
  }
}
