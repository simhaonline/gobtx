package com.gobtx.hub.bootstrap;

import com.gobtx.common.Utils;
import com.gobtx.hub.netty.HubChannelInitiator;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/** Created by Aaron Kuai on 2019/11/18. */
public class HubClientBootstrap extends AbstractLifecycleBootStrap {

  protected Channel rpcChannel;

  public HubClientBootstrap(
      final EventLoopGroup bossGroup,
      final InetSocketAddress address,
      final HubChannelInitiator channelInitiator,
      final BootStrapCallback callback) {

    super(bossGroup, null, address, channelInitiator, callback);
  }

  @Override
  protected void doStart() {

    // callback.update(this, Status.INIT);

    final Bootstrap b = new Bootstrap();
    b.group(bossGroup)
        .channel(Utils.IS_OS_LINUX ? EpollSocketChannel.class : NioSocketChannel.class)
        .remoteAddress(address)
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .option(ChannelOption.TCP_NODELAY, true);
    b.handler(channelInitiator).validate();

    callback.update(this, Status.INIT);
    final ChannelFuture connectFuture = b.connect();

    connectFuture.addListener(
        future -> {
          if (future.isSuccess()) {
            rpcChannel = connectFuture.channel();
            callback.update(HubClientBootstrap.this, Status.CONNECTED);

            rpcChannel
                .closeFuture()
                .addListener(closeFuture -> callback.update(HubClientBootstrap.this, Status.SHUT));

          } else {
            callback.update(HubClientBootstrap.this, Status.FAIL_CONNECT, future.cause());
            started.set(false);
          }
        });
  }

  @Override
  protected void doStop() {

    if (rpcChannel != null) {

      try {
        callback.update(HubClientBootstrap.this, Status.FORCE_SHUT);
        rpcChannel.close();
      } catch (Throwable throwable) {

      }
    }
  }
}
