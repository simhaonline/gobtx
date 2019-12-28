package com.gobtx.hub.netty;

import com.gobtx.hub.application.HubApplication;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Aaron Kuai on 2019/11/17. */
public class HubChannelInitiator extends ChannelInitializer<SocketChannel> {

  static final Logger logger = LoggerFactory.getLogger(HubChannelInitiator.class);

  protected final boolean debugEnable;

  protected final MessageToMessageDecoder decoder;

  protected final MessageToMessageEncoder encoder;

  protected final HubApplication application;

  public HubChannelInitiator(
      final boolean debugEnable,
      final MessageToMessageDecoder decoder,
      final MessageToMessageEncoder encoder,
      final HubApplication application) {

    this.debugEnable = debugEnable;
    this.decoder = decoder;
    this.encoder = encoder;
    this.application = application;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {

    ChannelPipeline pipeline = ch.pipeline();

    pipeline.addLast("pbFrameDecoder", new ProtobufVarint32FrameDecoder()); // Size is 4k at most

    pipeline.addLast("decoder", decoder);

    pipeline.addLast("pbLengthPrepender", new ProtobufVarint32LengthFieldPrepender());

    pipeline.addLast("encoder", encoder);

    if (debugEnable) {

      logger.warn("NETTY_LOG_ENABLE_FLUSH_TO  hub.network");
      pipeline.addLast("logging", new LoggingHandler("hub.network", LogLevel.DEBUG));
    }

    pipeline.addLast("idle", new IdleStateHandler(60, 60, 0)).addLast("application", application);
  }
}
