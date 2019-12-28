package com.gobtx.frontend.ws.push;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/** Created by Aaron Kuai on 2019/11/20. */
public class PushEvent {

  protected int segment;
  protected Channel channel;
  protected ByteBuf buf;

  public PushEvent reset(int segment, Channel channel, ByteBuf buf) {
    this.segment = segment;
    this.channel = channel;
    this.buf = buf;
    return this;
  }
}
