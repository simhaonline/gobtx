package com.gobtx.frontend.ws.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Aaron Kuai on 2019/11/19. */
public abstract class WebSocketApplication extends SimpleChannelInboundHandler<WebSocketFrame> {

  final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("WS_UN_EXCEPTION  {},{},{}", isClient(), ctx, cause);
  }

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

  protected abstract void handleReadIdle(final ChannelHandlerContext ctx);

  protected abstract void handleWriteIdle(final ChannelHandlerContext ctx);

  protected boolean isClient() {
    return false;
  }
}
