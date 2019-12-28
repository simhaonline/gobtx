package com.gobtx.frontend.ws.config;

/** Created by Aaron Kuai on 2019/11/19. */
public interface RequestPath {

  String SUBSCRIBE = "subscribe";
  String UNSUBSCRIBE = "unsubscribe";

  String KLINE = "kline";

  String LOGIN = "login";
  String LOGOUT = "logout";

  // this mean the user entry a specific symbol's
  // workshop's panel
  String WORKSHOP = "workshop";
  String HEARTBEAT = "heartbeat";
}
