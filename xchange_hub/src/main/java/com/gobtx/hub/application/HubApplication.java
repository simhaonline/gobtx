package com.gobtx.hub.application;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Aaron Kuai on 2019/11/18. */
public abstract class HubApplication<T> extends SimpleChannelInboundHandler<T> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    if (evt instanceof IdleStateEvent) {
      IdleStateEvent e = (IdleStateEvent) evt;
      if (e.state() == IdleState.READER_IDLE) {
        handleReadIdle(ctx);
      } else if (e.state() == IdleState.WRITER_IDLE) {
        handleWriteIdle(ctx);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.warn("UN_EXCEPTION  {},{},{}", isClient(), ctx, cause);
    // ctx.close();
  }

  protected void handleReadIdle(final ChannelHandlerContext ctx) {}

  protected void handleWriteIdle(final ChannelHandlerContext ctx) {}

  protected boolean isClient() {
    return true;
  }
}
