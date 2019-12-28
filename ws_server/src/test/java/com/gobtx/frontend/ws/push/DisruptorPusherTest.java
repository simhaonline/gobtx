package com.gobtx.frontend.ws.push;

import com.gobtx.frontend.ws.json.JSonHelper;
import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.TradeEventWithExchangeDataImpl;
import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

import static java.util.concurrent.TimeUnit.MINUTES;

/** Created by Aaron Kuai on 2019/11/21. */
public class DisruptorPusherTest {

  @Test
  public void testNettyEncodeGson2() throws IOException {

    TradeEventWithExchangeDataImpl data =
        new TradeEventWithExchangeDataImpl()
            .setExchange(Exchange.HUOBI)
            .setBuy(true)
            .setBuyerMaker(false)
            .setPrice(new BigDecimal("123123213213.21321312"))
            .setQuantity(new BigDecimal("0.1231321321321132131231"))
            .setReportTimestamp(System.currentTimeMillis())
            .setTradeTimestamp(System.currentTimeMillis());

    Gson gson =
        new GsonBuilder()
            // .serializeNulls()
            .setPrettyPrinting()
            .create();

    final ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(128);

    final String payload = gson.toJson(data);

    System.out.println(payload);

    JSonHelper.encodeTradeEvent(data, buf);

    System.out.println(">>>>>>>>>>>\n\n" + buf.toString(Charset.defaultCharset()));
  }

  @Test
  public void testNettyEncodeGson() throws IOException {

    OHLCData ohlcData =
        new OHLCDataImpl()
            .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(5))
            .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(4))
            .setOpen(BigDecimal.valueOf(123.11))
            .setHigh(BigDecimal.valueOf(125.88))
            .setLow(BigDecimal.valueOf(120.34))
            .setVolume(BigDecimal.valueOf(2323233))
            .setClose(BigDecimal.valueOf(124.12))
            .setNumberOfTrades(1323232l)
            .setTimeKey(123131231l)
            .setSymbol("USDJPY")
            .setAmount(new BigDecimal("23132133321233.232132131"));

    OHLCWithExchangeAndInternalData data =
        new OHLCWithExchangeAndInternalData(Exchange.BINANCE, KlineInterval.m1, ohlcData);

    Gson gson =
        new GsonBuilder()
            // .serializeNulls()
            .setPrettyPrinting()
            .create();

    final ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(128);

    final String payload = gson.toJson(data);

    JSonHelper.encodeMarketData(data, buf);

    // buf.writeCharSequence(payload, StandardCharsets.UTF_8);

    //
    //    System.out.println(gson.toJson(data));
    //
    //    JsonWriter writer = new JsonWriter(new OutputStreamWriter(new ByteBufOutputStream(buf)));
    //
    //    gson.toJson(data, OHLCWithExchangeAndInternalData.class, writer);
    //
    //    writer.flush();

    System.out.println(buf.readableBytes());

    System.out.println(">>>>>>>>>>>\n\n" + buf.toString(Charset.defaultCharset()));
  }
}
