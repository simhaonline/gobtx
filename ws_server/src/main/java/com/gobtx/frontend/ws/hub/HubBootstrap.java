package com.gobtx.frontend.ws.hub;

import io.netty.channel.EventLoopGroup;

/** Created by Aaron Kuai on 2019/11/26. */
public interface HubBootstrap {

  void start(EventLoopGroup workerEventloop);

  void stop();
}
