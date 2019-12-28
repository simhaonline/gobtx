package com.gobtx.hub.bootstrap;

import com.gobtx.hub.netty.HubChannelInitiator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import static com.gobtx.common.Utils.IS_OS_LINUX;

/** Created by Aaron Kuai on 2019/11/18. */
@SuppressWarnings("Duplicates")
public class HubServerBootstrap extends AbstractLifecycleBootStrap {

  private Channel rpcServerChannel;

  public HubServerBootstrap(
      final EventLoopGroup bossGroup,
      final EventLoopGroup workerGroup,
      final InetSocketAddress address,
      final HubChannelInitiator channelInitiator,
      final BootStrapCallback callback) {
    super(bossGroup, workerGroup, address, channelInitiator, callback);
  }

  @Override
  protected void doStart() throws InterruptedException {

    final ServerBootstrap bootstrap = new ServerBootstrap();

    callback.update(this, Status.NEW);

    bootstrap
        .group(bossGroup, workerGroup)
        .channel(IS_OS_LINUX ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
        .localAddress(address)
        .childHandler(channelInitiator);

    bootstrap.validate();

    callback.update(this, Status.INIT);
    ChannelFuture f = bootstrap.bind().sync();

    logger.warn("RPC_SERVER_KICK_AT {}", f.channel().localAddress());

    f.addListener(
        future -> {
          if (future.isSuccess()) {
            rpcServerChannel = f.channel();
            callback.update(HubServerBootstrap.this, Status.READY);

            rpcServerChannel
                .closeFuture()
                .addListener(
                    closeFuture ->
                        callback.update(
                            HubServerBootstrap.this, Status.FORCE_SHUT, closeFuture.cause()));

          } else {
            logger.error("FAIL_START_RPC_SERVER {}", future.cause());
            started.set(false);
            callback.update(HubServerBootstrap.this, Status.FAIL_INIT, future.cause());
          }
        });
  }

  @Override
  protected void doStop() {

    if (rpcServerChannel != null) {

      try {
        logger.warn("STOP_RPC");
        rpcServerChannel.close().sync();
        rpcServerChannel = null;
      } catch (InterruptedException e) {
        logger.error("STOP_SOCKET_SERVER_INTERRUPTED {}", e);
      }
    }
  }
}
