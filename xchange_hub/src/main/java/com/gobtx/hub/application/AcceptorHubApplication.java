package com.gobtx.hub.application;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/** Created by Aaron Kuai on 2019/11/18. */
@ChannelHandler.Sharable
public abstract class AcceptorHubApplication<T> extends HubApplication<T> {

  protected final AcceptorContextManager acceptorContextManager;

  protected AcceptorHubApplication(final AcceptorContextManager acceptorContextManager) {
    this.acceptorContextManager = acceptorContextManager;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    // Call twice I not sure why?
    acceptorContextManager.register(ctx);
    logger.debug("CLIENT_ACTIVE {}", ctx);
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) {
    logger.debug("CLIENT_REGISTER {}", ctx);
  }

  @Override
  protected boolean isClient() {
    return false;
  }
}
