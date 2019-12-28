package com.gobtx.frontend.ws.push;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Aaron Kuai on 2019/11/20. */
public class PushEventHandler implements EventHandler<PushEvent>, LifecycleAware {

  static final Logger logger = LoggerFactory.getLogger(PushEventHandler.class);

  protected final int segmentId;

  public PushEventHandler(int segmentId) {
    this.segmentId = segmentId;
  }

  @Override
  public void onEvent(PushEvent event, long sequence, boolean batch) throws Exception {
    if (event.segment != segmentId) return;

    try {

      if (event.channel.isWritable()) {
        event.channel.writeAndFlush(
            new TextWebSocketFrame(event.buf.retainedDuplicate()), event.channel.voidPromise());
      }

    } catch (Throwable e) {
      logger.warn("FAIL_FLUSH_SOCKET_CHANNEL {}", ExceptionUtils.getStackTrace(e));
    }
  }

  @Override
  public void onStart() {
    logger.warn("PUSH_HANDLER_START {}", segmentId);
  }

  @Override
  public void onShutdown() {
    logger.warn("PUSH_HANDLER_STOP {}", segmentId);
  }
}
