package com.gobtx.frontend.ws.json;

import com.gobtx.common.web.model.request.RequestPacket;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import com.gobtx.model.view.TradeEventWithExchangeView;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.gobtx.frontend.ws.json.JSonHelper.FieldShortName.*;

/** Created by Aaron Kuai on 2019/11/26. */
public class JSonHelper {

  static final Charset UTF_8 = StandardCharsets.UTF_8;

  interface FieldShortName {

    byte[] MESSAGE_TYPE = "\"T\":".getBytes(UTF_8);

    byte[] EXCHANGE = "\"eX\":".getBytes(UTF_8);
    byte[] REPORT_TIMESTAMP = "\"rT\":".getBytes(UTF_8);
    byte[] TRADE_TIMESTAMP = "\"tT\":".getBytes(UTF_8);
    byte[] PRICE = "\"p\":".getBytes(UTF_8);
    byte[] QUANTITY = "\"q\":".getBytes(UTF_8);
    byte[] BUY = "\"B\":".getBytes(UTF_8);
    byte[] BUY_MAKER = "\"BM\":".getBytes(UTF_8);

    byte[] INTERVAL = "\"I\":".getBytes(UTF_8);
    byte[] SYMBOL = "\"s\":".getBytes(UTF_8);
    byte[] TIME_KEY = "\"tK\":".getBytes(UTF_8);
    byte[] OPEN_TIME = "\"oT\":".getBytes(UTF_8);
    byte[] CLOSE_TIME = "\"cT\":".getBytes(UTF_8);
    byte[] OPEN = "\"o\":".getBytes(UTF_8);
    byte[] HIGH = "\"h\":".getBytes(UTF_8);
    byte[] LOW = "\"l\":".getBytes(UTF_8);
    byte[] CLOSE = "\"c\":".getBytes(UTF_8);
    byte[] VOLUME = "\"v\":".getBytes(UTF_8);
    byte[] AMOUNT = "\"a\":".getBytes(UTF_8);
    byte[] NUMBER_OF_TRADER = "\"NOT\":".getBytes(UTF_8);

    //
    byte LEFT_BRACE = '{';
    byte RIGHT_BRACE = '}';
    byte COMMA = ',';
    // byte SEMICOLON = ';';
    byte LEFT_BRACKET = '[';
    byte RIGHT_BRACKET = ']';
    byte QUOTE = '"';

    byte B_TRUE = '1';
    byte B_FALSE = '0';

    byte[] REQUEST_ID = "\"RID\":".getBytes(UTF_8);
    byte[] PATH = "\"PATH\":".getBytes(UTF_8);
    byte[] ERROR_CODE = "\"E_CODE\":".getBytes(UTF_8);
    byte[] ERROR_MSG = "\"E_MSG\":".getBytes(UTF_8);
  }

  static final byte[] ERR_MSG = "\"X\"".getBytes(UTF_8);

  public static ByteBuf failRequest(
      final RequestPacket request, final int errorCode, final String errorMsg, final ByteBuf buf) {

    buf.writeByte(LEFT_BRACE);

    buf.writeBytes(MESSAGE_TYPE).writeBytes(ERR_MSG).writeByte(COMMA);

    buf.writeBytes(REQUEST_ID).writeCharSequence(Long.toString(request.getRequestId()), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(PATH).writeByte(QUOTE).writeCharSequence(request.getPath(), UTF_8);
    buf.writeByte(QUOTE);
    buf.writeByte(COMMA);

    if (errorMsg != null && !errorMsg.isEmpty()) {

      buf.writeBytes(ERROR_CODE).writeCharSequence(Integer.toString(errorCode), UTF_8);
      buf.writeByte(COMMA);

      buf.writeBytes(ERROR_MSG).writeByte(QUOTE).writeCharSequence(errorMsg, UTF_8);
      buf.writeByte(QUOTE);

    } else {
      buf.writeBytes(ERROR_CODE).writeCharSequence(Integer.toString(errorCode), UTF_8);
    }

    buf.writeByte(RIGHT_BRACE);

    return buf;
  }

  static final byte MKT_EVT = '1';

  public static void encodeMarketData(
      final OHLCWithExchangeAndIntervalView data, final ByteBuf buf) {

    // {
    //  "exchange": "BINANCE",
    //  "interval": "m1",
    //  "data": {
    //    "symbol": "USDJPY",
    //    "timeKey": 123131231,
    //    "openTime": 1574720884004,
    //    "closeTime": 1574720944004,
    //    "open": 123.11,
    //    "high": 125.88,
    //    "low": 120.34,
    //    "close": 124.12,
    //    "volume": 2323233,
    //    "amount": 23132133321233.232132131,
    //    "numberOfTrades": 1323232
    //  }
    // }

    // {
    //  "T": 1,
    //  "eX": "BINANCE",
    //  "I": "m1",
    //  "s": "USDJPY",
    //  "tK": 123131231,
    //  "oT": 1574742651561,
    //  "cT": 1574742711561,
    //  "o": 123.11,
    //  "h": 125.88,
    //  "l": 120.34,
    //  "c": 124.12,
    //  "v": 2323233,
    //  "a": 23132133321233.23,
    //  "NOT": 1323232
    // }

    buf.writeByte(LEFT_BRACE);

    buf.writeBytes(MESSAGE_TYPE).writeByte(MKT_EVT).writeByte(COMMA);

    buf.writeBytes(EXCHANGE).writeBytes(data.getExchange().getNameBytes()).writeByte(COMMA);

    buf.writeBytes(INTERVAL).writeBytes(data.getInterval().getNameBytes()).writeByte(COMMA);

    buf.writeBytes(SYMBOL);
    buf.writeByte(QUOTE).writeCharSequence(data.getSymbol(), UTF_8);
    buf.writeByte(QUOTE).writeByte(COMMA);

    buf.writeBytes(TIME_KEY).writeCharSequence(Long.toString(data.getTimeKey()), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(OPEN_TIME).writeCharSequence(Long.toString(data.getOpenTime()), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(CLOSE_TIME).writeCharSequence(Long.toString(data.getCloseTime()), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(OPEN);
    buf.writeCharSequence(data.getOpen().toString(), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(HIGH);
    buf.writeCharSequence(data.getHigh().toString(), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(LOW);
    buf.writeCharSequence(data.getLow().toString(), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(CLOSE);
    buf.writeCharSequence(data.getClose().toString(), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(VOLUME);
    buf.writeCharSequence(data.getVolume().toString(), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(AMOUNT);
    buf.writeCharSequence(data.getAmount().toString(), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(NUMBER_OF_TRADER)
        .writeCharSequence(Long.toString(data.getNumberOfTrades()), UTF_8);

    buf.writeByte(RIGHT_BRACE);
  }

  static final byte TRADE_EVT = '2';
  //
  public static void encodeTradeEvent(final TradeEventWithExchangeView data, final ByteBuf buf) {

    // {
    //  "exchange": "HUOBI",
    //  "reportTimestamp": 1574738268151,
    //  "tradeTimestamp": 1574738268151,
    //  "price": 123123213213.21321312,
    //  "quantity": 0.1231321321321132131231,
    //  "buyerMaker": false,
    //  "buy": true
    // }

    // {
    //  "T": 2,
    //  "eX": "HUOBI",
    //  "rT": 1574742897068,
    //  "tT": 1574742897068,
    //  "p": 123123213213.21321,
    //  "q": 0.12313213213211321,
    //  "BM": 0,
    //  "B": 1
    // }

    buf.writeByte(LEFT_BRACE);

    buf.writeBytes(MESSAGE_TYPE).writeByte(TRADE_EVT).writeByte(COMMA);

    buf.writeBytes(SYMBOL);
    buf.writeByte(QUOTE).writeCharSequence(data.getSymbol(), UTF_8);
    buf.writeByte(QUOTE).writeByte(COMMA);

    buf.writeBytes(EXCHANGE).writeBytes(data.getExchange().getNameBytes()).writeByte(COMMA);

    // buf.writeBytes(REPORT_TIMESTAMP);
    // buf.writeCharSequence(Long.toString(data.getReportTimestamp()), UTF_8);
    // buf.writeByte(COMMA);

    buf.writeBytes(TRADE_TIMESTAMP);
    buf.writeCharSequence(Long.toString(data.getTradeTimestamp()), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(PRICE);
    buf.writeCharSequence(data.getPrice().toString(), UTF_8);
    buf.writeByte(COMMA);

    buf.writeBytes(QUANTITY);
    buf.writeCharSequence(data.getQuantity().toString(), UTF_8);
    // buf.writeByte(COMMA);

    // buf.writeBytes(BUY_MAKER);
    // buf.writeByte(data.isBuyerMaker() ? B_TRUE : B_FALSE);
    // buf.writeByte(COMMA);

    // buf.writeBytes(BUY);
    // buf.writeByte(data.isBuy() ? B_TRUE : B_FALSE);

    buf.writeByte(RIGHT_BRACE);
  }
}
