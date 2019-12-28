package com.gobtx.hub.netty;

import com.gobtx.hub.protocol.CodecHelper;
import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.TradeEventWithExchangeDataImpl;
import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.gobtx.common.Constants.MKT_DATA;
import static com.gobtx.common.Constants.TRADE_DATA;

/** Created by Aaron Kuai on 2019/11/18. */
@ChannelHandler.Sharable
public class HubMarketDataMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

  static final Logger logger = LoggerFactory.getLogger(HubMarketDataMessageDecoder.class);

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

    if (in.isReadable()) {

      in.markReaderIndex();
      final byte messageType = in.readByte();

      switch (messageType) {
        case MKT_DATA:
          final Exchange exchange = Exchange.fromOrdinal(in.readByte());
          final KlineInterval klineInterval = KlineInterval.fromOrdinal(in.readByte());

          final OHLCData data = new OHLCDataImpl();

          final OHLCWithExchangeAndIntervalView view =
              new OHLCWithExchangeAndInternalData(exchange, klineInterval, data);

          CodecHelper.decodeTradeEvent(in, data);

          out.add(view);
          return;

        case TRADE_DATA:
          final Exchange tradeExchange = Exchange.fromOrdinal(in.readByte());
          final TradeEventWithExchangeDataImpl tradeData =
              new TradeEventWithExchangeDataImpl().setExchange(tradeExchange);

          out.add(CodecHelper.decodeTradeEvent(in, tradeData));

          return;
        default:
          logger.error("DECO_MET_UN_KNOWN_TYPE_MSG  {}", messageType);
          in.resetReaderIndex();
          return;
      }
    }
  }
}
