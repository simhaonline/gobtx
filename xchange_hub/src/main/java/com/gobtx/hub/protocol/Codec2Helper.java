package com.gobtx.hub.protocol;

import com.gobtx.model.domain.TradeEventData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.view.TradeEventView;
import io.netty.buffer.ByteBuf;

/** Created by Aaron Kuai on 2019/11/25. */
public class Codec2Helper {

  public static void encode(final Exchange exchange, final TradeEventView event) {}

  public static TradeEventData decode(final ByteBuf buf, final TradeEventData data) {

    return data;
  }
}
