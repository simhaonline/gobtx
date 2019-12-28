package com.gobtx.frontend.ws.config.reactive;

import com.gobtx.common.Env;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.netty.http.server.HttpServer;

import java.net.InetSocketAddress;

/** Created by Aaron Kuai on 2019/11/8. */
@Profile("reactive")
@Component
public class NettyWebServerFactory {

  // server.port

  @Value("#{systemProperties['LOCAL_ADDRESS'] ?: ''}")
  public String localAddress;

  @Value("${server.port:8088}")
  public int serverPort;

  static final Logger logger = LoggerFactory.getLogger(NettyWebServerFactory.class);

  @Bean
  public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {

    NettyReactiveWebServerFactory webServerFactory = new NettyReactiveWebServerFactory();

    InetSocketAddress address;

    if (serverPort <= 8000) {
      logger.warn("NO_SERVER_PORT_INJECT {}", serverPort);
      serverPort = 8088;
    }

    if (Env.isProd()) {
      logger.warn("PROD_ENV_USER_INNER_IP_FOR_WEB {}", localAddress);
      address = new InetSocketAddress(localAddress, serverPort);
    } else {
      address = new InetSocketAddress(serverPort);
    }

    logger.warn("WEB_SERVER_HOST_ON {}", address);

    webServerFactory.addServerCustomizers(new EventLoopNettyCustomizer(address));

    return webServerFactory;
  }

  /**
   * @see reactor.netty.resources.DefaultLoopResources
   * @see reactor.netty.resources.DefaultLoopEpoll
   * @see reactor.netty.http.server.HttpServer
   * @see reactor.netty.http.server.HttpServerBind
   * @see reactor.netty.resources.DefaultLoopNativeDetector
   */
  private static class EventLoopNettyCustomizer implements NettyServerCustomizer {

    protected final InetSocketAddress address;

    public EventLoopNettyCustomizer(InetSocketAddress address) {
      this.address = address;
    }

    @Override
    public HttpServer apply(HttpServer httpServer) {

      // See the @see the reactor.netty already done most of the thing

      //            EventLoopGroup bossGroup, workerGroup;
      //
      //            if (SystemUtils.IS_OS_LINUX) {
      //                logger.warn("RUNNING_ON_LINUX_EPOLL_ENABLE");
      //                bossGroup = new EpollEventLoopGroup();
      //                workerGroup = new
      // EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
      //
      //            } else {
      //                logger.warn("RUNNING_ON_NO_LINUX_EPOLL_DISABLE");
      //                bossGroup = new NioEventLoopGroup();
      //                workerGroup = new
      // NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
      //            }

      // httpServer.port()
      // Ssl ssl = new Ssl();
      //        ssl.setEnabled(true);
      //        ssl.setKeyStore("classpath:sample.jks");
      //        ssl.setKeyAlias("alias");
      //        ssl.setKeyPassword("password");
      //        ssl.setKeyStorePassword("secret");
      //        Http2 http2 = new Http2();
      //        http2.setEnabled(false);
      //        serverFactory.addServerCustomizers(new SslServerCustomizer(ssl, http2, null));
      //        serverFactory.setPort(8443);

      return httpServer.tcpConfiguration(
          tcpServer ->
              tcpServer.bootstrap(
                  serverBootstrap ->
                      serverBootstrap
                          // .group(bossGroup, workerGroup)
                          // .channel(SystemUtils.IS_OS_LINUX ? EpollServerSocketChannel.class :
                          // NioServerSocketChannel.class)
                          .localAddress(address)
                          .childOption(ChannelOption.TCP_NODELAY, true)
                          .childOption(ChannelOption.SO_KEEPALIVE, true)
                          .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true)))

          // TODO bind to only inner address
          // .addressSupplier()

          );
    }
  }
}
