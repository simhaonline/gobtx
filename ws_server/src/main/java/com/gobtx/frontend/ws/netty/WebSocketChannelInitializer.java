package com.gobtx.frontend.ws.netty;

import com.google.gson.Gson;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Aaron Kuai on 2019/11/18. */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

  protected final Logger logger = LoggerFactory.getLogger(WebSocketChannelInitializer.class);

  protected final boolean debugEnable;

  protected final Gson gson;

  public WebSocketChannelInitializer(final boolean debugEnable, Gson gson) {
    this.debugEnable = debugEnable;
    this.gson = gson;
  }

  @Override
  public void initChannel(final SocketChannel ch) throws Exception {

    final ChannelPipeline pipeline = ch.pipeline();

    if (debugEnable) {
      logger.warn("WEB_SOCKET_LOGGER_ENABLE ");
      pipeline.addLast("logger", new LoggingHandler("websocket.data", LogLevel.DEBUG));
    }

    pipeline
        .addLast("httpServerCodec", new HttpServerCodec())
        .addLast("chunkedWriteHandler", new ChunkedWriteHandler())
        .addLast("httpObjectAggregator", new HttpObjectAggregator(65536))
        .addLast("webSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/ws"))
        // How to generic decoder the data it is binary or text let the application do those stuff
        .addLast("application", new AcceptorWebSocketApplication(gson))
        .addLast("idle", new IdleStateHandler(200, 200, 0));
  }
}
