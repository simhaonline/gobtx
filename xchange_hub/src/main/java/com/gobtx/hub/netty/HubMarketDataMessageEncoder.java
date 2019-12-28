package com.gobtx.hub.netty;

import com.gobtx.hub.protocol.CodecHelper;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.gobtx.common.Constants.MKT_DATA;

/**
 * Created by Aaron Kuai on 2019/11/18.
 *
 * @see com.gobtx.model.view.OHLCWithExchangeAndIntervalView
 */
@ChannelHandler.Sharable
public class HubMarketDataMessageEncoder
    extends MessageToMessageEncoder<OHLCWithExchangeAndIntervalView> {

  static final Logger logger = LoggerFactory.getLogger(HubMarketDataMessageEncoder.class);

  @Override
  protected void encode(
      ChannelHandlerContext ctx, OHLCWithExchangeAndIntervalView view, List<Object> out)
      throws Exception {

    final ByteBuf db = ctx.alloc().directBuffer(128);

    db.writeByte(MKT_DATA);
    db.writeByte(view.getExchange().ordinal());
    db.writeByte(view.getInterval().ordinal());
    CodecHelper.encodeTradeEvent(db, view);

    out.add(db);
  }
}
