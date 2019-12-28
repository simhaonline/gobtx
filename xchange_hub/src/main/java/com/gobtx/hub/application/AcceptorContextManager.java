package com.gobtx.hub.application;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/** Created by Aaron Kuai on 2019/11/18. */
public class AcceptorContextManager {
  // This used for internal

  static final Logger logger = LoggerFactory.getLogger(AcceptorContextManager.class);

  private final Map<ChannelId, ChannelHandlerContext> registered = new ConcurrentHashMap<>(32);

  private static final AcceptorContextManager INSTANCE = new AcceptorContextManager();

  public static AcceptorContextManager getInstance() {
    return INSTANCE;
  }

  private AcceptorContextManager() {}

  public void register(final ChannelHandlerContext context) {

    final ChannelId channelId = context.channel().id();

    context
        .channel()
        .closeFuture()
        .addListener(
            future -> {
              logger.warn("CLIENT_CLOSED {}", channelId);
              registered.remove(channelId);
            });

    registered.put(channelId, context);
  }

  public boolean isEmpty() {
    return registered.isEmpty();
  }

  public Map<ChannelId, ChannelHandlerContext> getRegistered() {
    return registered;
  }

  public Stream<ChannelHandlerContext> subscribes() {

    if (registered.isEmpty()) return Stream.empty();

    return registered.values().stream().filter(it -> it.channel().isWritable());
  }
}
