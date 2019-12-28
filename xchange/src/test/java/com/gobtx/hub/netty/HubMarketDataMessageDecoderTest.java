package com.gobtx.hub.netty;

import com.gobtx.hub.protocol.CodecHelper;
import com.gobtx.model.domain.TradeEventDataImpl;
import com.gobtx.model.enums.Exchange;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.gobtx.common.Constants.TRADE_DATA;

/** Created by Aaron Kuai on 2019/12/27. */
public class HubMarketDataMessageDecoderTest {

  /**
   * com.gobtx.xchange.Bootstrap#TradeDataStreamListener's encode and HubMarketDataMessageDecoder's
   * decode logic
   */
  @Test
  public void decode() {

    TradeEventDataImpl event = new TradeEventDataImpl();

    event
        .setSymbol("BTCUSDT")
        .setBuy(true)
        .setBuyerMaker(false)
        .setPrice(new BigDecimal("23213.11"))
        .setQuantity(new BigDecimal("12.234"))
        .setReportTimestamp(System.currentTimeMillis())
        .setTradeTimestamp(System.currentTimeMillis());

    final ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer(128);

    buffer.writeByte(TRADE_DATA);
    buffer.writeByte(Exchange.HUOBI.ordinal());

    CodecHelper.encodeTradeEvent(buffer, event);

    HubMarketDataMessageDecoder decoder = new HubMarketDataMessageDecoder();

    List res = new ArrayList();

    decoder.decode(null, buffer, res);

    System.out.println(res);
  }
}
