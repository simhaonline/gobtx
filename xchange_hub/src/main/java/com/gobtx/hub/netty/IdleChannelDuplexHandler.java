package com.gobtx.hub.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/** Created by Aaron Kuai on 2019/11/17. */
public class IdleChannelDuplexHandler extends ChannelDuplexHandler {

  @Override
  public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt)
      throws Exception {

    if (evt instanceof IdleStateEvent) {
      IdleStateEvent e = (IdleStateEvent) evt;
      if (e.state() == IdleState.READER_IDLE) {

        ctx.close();

      } else if (e.state() == IdleState.WRITER_IDLE) {
        // ctx.writeAndFlush(new PingMessage());
      }
    }
  }
}
