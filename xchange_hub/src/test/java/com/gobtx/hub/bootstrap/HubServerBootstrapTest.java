package com.gobtx.hub.bootstrap;

import com.gobtx.hub.Utils;
import com.gobtx.hub.application.AcceptorContextManager;
import com.gobtx.hub.application.AcceptorHubApplication;
import com.gobtx.hub.application.HubApplication;
import com.gobtx.hub.netty.HubChannelInitiator;
import com.gobtx.hub.netty.HubMarketDataMessageDecoder;
import com.gobtx.hub.netty.HubMarketDataMessageEncoder;
import com.gobtx.hub.protocol.CodecHelper;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.gobtx.common.Utils.IS_OS_LINUX;

/** Created by Aaron Kuai on 2019/11/18. */
public class HubServerBootstrapTest {

  static {
    InputStream in =
        HubServerBootstrapTest.class.getClassLoader().getResourceAsStream("log4j2.xml");

    //    final ConfigurationSource source;
    //    try {
    //      source = new ConfigurationSource(in);
    //      Configurator.initialize(null, source);
    //    } catch (IOException e) {
    //      e.printStackTrace();
    //      System.exit(1);
    //    }
  }

  public static EventLoopGroup bossGroup;

  public static EventLoopGroup workerGroup;

  public static Logger logger;

  @BeforeClass
  public static void doClzInit() {

    logger = LoggerFactory.getLogger(HubServerBootstrapTest.class);

    bossGroup = IS_OS_LINUX ? new EpollEventLoopGroup(4) : new NioEventLoopGroup(4);
    workerGroup = bossGroup;
  }

  @Test
  public void doStart() throws IOException, InterruptedException {

    InetAddress inetAddress = InetAddress.getLocalHost();

    InetSocketAddress address = new InetSocketAddress(10241);

    HubChannelInitiator hubChannelInitiator =
        new HubChannelInitiator(
            false,
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

                logger.warn("CLIENT_READ_IN>>>>>>>>>>> {}", msg);
              }
            });

    final CountDownLatch countDownLatch = new CountDownLatch(1);

    HubServerBootstrap bootstrap =
        new HubServerBootstrap(
            bossGroup,
            workerGroup,
            address,
            hubChannelInitiator,
            new BootStrapCallback() {
              @Override
              public void update(LifecycleBootStrap bootStrap, Status status, Object... args) {
                logger.warn("EVENT {},{}", bootStrap, status);

                if (Status.READY == status) {

                  countDownLatch.countDown();
                }
              }
            });

    bootstrap.start();

    try {
      countDownLatch.await(5, TimeUnit.SECONDS);
    } catch (Throwable throwable) {
      logger.error("fail start {}", throwable);
      Assert.fail("FAIL START  " + throwable.getMessage());
    }

    System.out.println("HEEEEEEEEEEEEEEEEER");

    HubChannelInitiator clientHubChannelInitiator =
        new HubChannelInitiator(
            false,
            new HubMarketDataMessageDecoder(),
            new HubMarketDataMessageEncoder(),
            new HubApplication<OHLCWithExchangeAndIntervalView>() {

              @Override
              protected void handleReadIdle(ChannelHandlerContext ctx) {
                logger.warn("CLIENT_READ_IDLE {}", ctx);
              }

              @Override
              protected void handleWriteIdle(ChannelHandlerContext ctx) {
                logger.warn("CLIENT_WRITE_IDLE {}", ctx);
              }

              @Override
              protected void channelRead0(
                  ChannelHandlerContext ctx, OHLCWithExchangeAndIntervalView msg) throws Exception {

                logger.warn("CLIENT_READ_IN {}", msg);
              }
            });

    CountDownLatch countDownLatch1 = new CountDownLatch(1);
    HubClientBootstrap clientBootstrap =
        new HubClientBootstrap(
            bossGroup,
            address,
            clientHubChannelInitiator,
            new BootStrapCallback() {
              @Override
              public void update(LifecycleBootStrap bootStrap, Status status, Object... args) {

                logger.debug("LOCAL_STATUS {}", status);
                if (Status.CONNECTED == status) {

                  logger.warn("IT IS READY !!");
                  countDownLatch1.countDown();
                }
              }
            });

    clientBootstrap.start();
    try {
      countDownLatch1.await(3, TimeUnit.SECONDS);
    } catch (Throwable throwable) {
      Assert.fail(throwable.getMessage());
    }

    logger.warn("Start to do the publish ");

    final List<OHLCDataImpl> testDataList = Utils.loadTestData();

    OHLCDataImpl lastdt = null;
    for (OHLCDataImpl dt : testDataList) {
      lastdt = dt;
      AcceptorContextManager.getInstance()
          .subscribes()
          .forEach(
              it -> {
                logger.warn("TRY_PUBLISH {}", it, dt.getOpenTime());
                it.writeAndFlush(
                    new OHLCWithExchangeAndInternalData(Exchange.BINANCE, KlineInterval.d1, dt),
                    it.voidPromise());
              });
    }

    ByteBuf db = PooledByteBufAllocator.DEFAULT.directBuffer(128);

    db.writeByte(Exchange.HUOBI.ordinal());
    db.writeByte(KlineInterval.d3.ordinal());
    CodecHelper.encodeTradeEvent(db, lastdt);

    // Raw byte buf also can work
    AcceptorContextManager.getInstance()
        .subscribes()
        .forEach(
            it -> {
              it.writeAndFlush(db.retainedDuplicate(), it.voidPromise());
            });

    Thread.sleep(5_000);
  }
}
