package com.gobtx.frontend.ws.hub;

import com.gobtx.common.executor.GlobalExecutorService;
import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.frontend.ws.push.LocalMarketDataLastSnapshotter;
import com.gobtx.frontend.ws.push.MarketDataLastSnapshotter;
import com.gobtx.hub.application.HubApplication;
import com.gobtx.hub.bootstrap.HubClientBootstrap;
import com.gobtx.hub.bootstrap.Status;
import com.gobtx.hub.netty.HubChannelInitiator;
import com.gobtx.hub.netty.HubMarketDataMessageDecoder;
import com.gobtx.hub.netty.HubMarketDataMessageEncoder;
import com.gobtx.model.statistic.Trade24HManager;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import com.gobtx.model.view.TradeEventWithExchangeView;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Created by Aaron Kuai on 2019/11/20. */
@Component
@Profile("rpc-hub")
public class MarketHubClientBootstrap implements HubBootstrap {

  static final Logger logger = LoggerFactory.getLogger(MarketHubClientBootstrap.class);

  // Hook the client to all the hub available internal

  protected final HubServerProperties properties;

  protected final ServerKeySupplier serverKeySupplier;

  protected MarketHubClientListener marketHubClientListener;

  protected MarketDataLastSnapshotter snapshotter;

  protected final Trade24HManager manager;

  // 0. init-ing
  // 1. fail
  // 2. ready

  final Map<ServerKey, ClientHubContext> serverStatus = new ConcurrentHashMap<>();

  HubChannelInitiator clientHubChannelInitiator;

  public MarketHubClientBootstrap(
      @Autowired HubServerProperties properties,
      @Autowired ServerKeySupplier serverKeySupplier,
      @Autowired(required = false) MarketDataLastSnapshotter snapshotter,
      @Autowired(required = false) MarketHubClientListener marketHubClientListener,
      @Autowired(required = false) Trade24HManager manager) {

    this.properties = properties;
    this.serverKeySupplier = serverKeySupplier;
    this.snapshotter = snapshotter;
    this.marketHubClientListener = marketHubClientListener;
    this.manager = manager;
  }

  static class ClientHubContext {
    final HubClientBootstrap hub;
    final AtomicInteger status;

    ClientHubContext(HubClientBootstrap hub, AtomicInteger status) {
      this.hub = hub;
      this.status = status;
    }
  }

  private void tryConnectHub(final ServerKey server) {

    final AtomicInteger statusFlag = new AtomicInteger(0);
    final HubClientBootstrap clientBootstrap =
        new HubClientBootstrap(
            eventLoopGroup,
            server.address(),
            clientHubChannelInitiator,
            (bootStrap, status, args) -> {
              logger.warn("MARKET_HUB_STATUS {},{}", server, status);

              if (Status.INIT == status) return;
              if (Status.CONNECTED == status) {
                statusFlag.set(2);
              } else {
                StringBuilder error = new StringBuilder("Error: ");
                if (args != null) {
                  for (final Object arg : args) {
                    error.append(arg.toString()).append("\n");
                  }
                }
                logger.warn("MARKET_HUB_FAIL {},{},{}", server, status, error.toString());
                statusFlag.set(1);
              }
            });

    serverStatus.put(server, new ClientHubContext(clientBootstrap, statusFlag));

    GlobalExecutorService.INSTANCE.submit(
        () -> {
          try {
            clientBootstrap.start();
            logger.warn("FINISH_KICK_CLIENT_JOIN {}", server);
          } catch (Throwable throwable) {
            logger.error(
                "UN_KNOWN_EXCEPTION {},{}", server, ExceptionUtils.getStackTrace(throwable));
            statusFlag.set(1);
          }
        });
  }

  protected EventLoopGroup eventLoopGroup;
  protected ScheduledFuture scheduledFuture;

  /** This is stateless so it must be sharable */
  @ChannelHandler.Sharable
  class ClientHubApplication extends HubApplication {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      MarketHubClientBootstrap.logger.warn(
          "HUB_CLIENT_EXCEPTION {},{}", ctx.channel(), ExceptionUtils.getStackTrace(cause));
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) {

      if (msg instanceof OHLCWithExchangeAndIntervalView) {
        OHLCWithExchangeAndIntervalView omsg = (OHLCWithExchangeAndIntervalView) msg;
        //        if (logger.isDebugEnabled()) {
        //          logger.debug(
        //              "GOT_OHLC {},{},{}", omsg.getExchange(), omsg.getInterval(),
        // omsg.getSymbol());
        //        }
        marketHubClientListener.handle(omsg);
        // snapshotter.handle((OHLCWithExchangeAndIntervalView) msg);
        manager.handle(omsg);

      } else if (msg instanceof TradeEventWithExchangeView) {
        TradeEventWithExchangeView tmsg = (TradeEventWithExchangeView) msg;
        //        if (logger.isDebugEnabled()) {
        //          logger.debug("GOT_TRADE {},{}", tmsg.getExchange(), tmsg.getSymbol());
        //        }
        marketHubClientListener.handleTradeEvent(tmsg);
      }
    }
  }

  public void start(final EventLoopGroup eventLoopGroup) {

    this.eventLoopGroup = eventLoopGroup;
    if (marketHubClientListener == null) {
      marketHubClientListener = new MockMarketHubClientListener();
    }
    if (snapshotter == null) {
      snapshotter = LocalMarketDataLastSnapshotter.INSTANCE;
    }

    clientHubChannelInitiator =
        new HubChannelInitiator(
            false,
            new HubMarketDataMessageDecoder(),
            new HubMarketDataMessageEncoder(),
            new ClientHubApplication());

    final Collection<ServerKey> servers = serverKeySupplier.servers();
    if (servers.isEmpty()) {
      logger.warn("NO_HUB_SERVER_NO_DATA_UPDATE");
    } else {

      for (final ServerKey server : servers) {
        tryConnectHub(server);
      }
    }

    // Watch Dog for each
    logger.warn("CLIENT_HUB_SCAN_FREQUENT {}s", properties.getScanFrequent());
    scheduledFuture =
        GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
            () -> {

              // 1. new add hub
              // 2. removed hub
              // 3. failed hub

              final Collection<ServerKey> updateServers = serverKeySupplier.servers();

              final Set<ServerKey> removed = new HashSet<>();
              final Set<ServerKey> added = new HashSet<>();
              final Set<ServerKey> failed = new HashSet<>();

              serverStatus
                  .keySet()
                  .forEach(
                      k -> {
                        if (!updateServers.contains(k)) {
                          removed.add(k);
                        }
                      });

              updateServers.forEach(
                  it -> {
                    if (!serverStatus.containsKey(it)) {
                      removed.add(it);
                    }
                  });

              serverStatus.forEach(
                  (k, b) -> {
                    if (b.status.get() != 2) {

                      if (removed.contains(k)) {
                        // ignore
                      } else {
                        failed.add(k);
                      }
                    } else {
                      // 0, is init
                      // 1. is still connecting
                    }
                  });

              if (!removed.isEmpty()) {

                for (ServerKey server : removed) {

                  ClientHubContext mbp = serverStatus.get(server);
                  if (mbp != null) {
                    logger.warn("TRY_STOP_REMOVED {}", server);
                    mbp.hub.stop();
                  }
                }
              }

              if (!added.isEmpty()) {

                for (ServerKey server : added) {
                  logger.warn("TRY_CONNECT_NEW_ADDED_HUB");
                  tryConnectHub(server);
                }
              }

              if (!failed.isEmpty()) {

                for (final ServerKey server : failed) {
                  ClientHubContext mbp = serverStatus.get(server);
                  if (mbp != null) {
                    logger.warn("TRY_STOP_FAILED {}", server);
                    mbp.hub.stop();
                  }
                  logger.warn("TRY_START_FAILED {}", server);
                  tryConnectHub(server);
                }
              }
            },
            properties.getScanFrequent(),
            properties.getScanFrequent(),
            TimeUnit.SECONDS);
  }

  public void stop() {

    if (scheduledFuture != null) {
      try {
        scheduledFuture.cancel(true);
      } catch (Throwable throwable) {

      }
    }

    serverStatus.forEach(
        (k, b) -> {
          try {
            b.hub.stop();
          } catch (Throwable throwable) {

          }
        });

    serverStatus.clear();
  }
}
