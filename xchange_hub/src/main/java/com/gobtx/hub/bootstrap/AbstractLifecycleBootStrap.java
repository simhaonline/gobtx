package com.gobtx.hub.bootstrap;

import com.gobtx.hub.netty.HubChannelInitiator;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/** Created by Aaron Kuai on 2019/11/18. */
public abstract class AbstractLifecycleBootStrap implements LifecycleBootStrap {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected final AtomicBoolean started = new AtomicBoolean(false);

  protected final EventLoopGroup bossGroup;

  protected final EventLoopGroup workerGroup;

  protected final InetSocketAddress address;

  protected final HubChannelInitiator channelInitiator;

  protected final BootStrapCallback callback;

  protected AbstractLifecycleBootStrap(
      final EventLoopGroup bossGroup,
      final EventLoopGroup workerGroup,
      final InetSocketAddress address,
      final HubChannelInitiator channelInitiator,
      final BootStrapCallback callback) {

    this.bossGroup = bossGroup;
    this.workerGroup = workerGroup;
    this.address = address;
    this.channelInitiator = channelInitiator;
    this.callback = callback;
  }

  @Override
  public void start() throws InterruptedException {

    if (started.compareAndSet(false, true)) {
      logger.warn("PREPARE_START_NETTY_BOOTSTRAP");
      doStart();
    }
  }

  protected abstract void doStart() throws InterruptedException;

  @Override
  public void stop() {

    if (started.compareAndSet(true, false)) {
      doStop();
    }
  }

  protected abstract void doStop();
}
