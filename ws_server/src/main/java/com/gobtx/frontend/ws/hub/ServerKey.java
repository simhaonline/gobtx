package com.gobtx.frontend.ws.hub;

import java.net.InetSocketAddress;

/** Created by Aaron Kuai on 2019/11/20. */
@FunctionalInterface
public interface ServerKey {
  InetSocketAddress address();
}
