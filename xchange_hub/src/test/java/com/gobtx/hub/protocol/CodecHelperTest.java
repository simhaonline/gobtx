package com.gobtx.hub.protocol;

import com.gobtx.model.domain.OHLCDataImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/18. */
public class CodecHelperTest {

  @Test
  public void encode() {

    OHLCDataImpl example =
        new OHLCDataImpl()
            .setSymbol("usdjpy")
            .setTimeKey(System.currentTimeMillis())
            .setOpenTime(System.currentTimeMillis())
            .setCloseTime(System.currentTimeMillis())
            .setOpen(BigDecimal.valueOf(23131.2313d))
            .setHigh(BigDecimal.valueOf(23131.2313d))
            .setLow(BigDecimal.valueOf(23131.2313d))
            .setClose(BigDecimal.valueOf(23131.2313d))
            .setAmount(BigDecimal.valueOf(23131313l))
            .setVolume(BigDecimal.valueOf(223028d))
            .setNumberOfTrades(System.currentTimeMillis());

    ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(128);

    int size = CodecHelper.encodeTradeEvent(buf, example);

    System.out.println("Read Index::: " + buf.readerIndex());
    System.out.println("Write Index::: " + buf.writerIndex());

    System.out.println("SIZE::: " + size);
    System.out.println("Read Index::: " + buf.readerIndex());

    OHLCDataImpl res = new OHLCDataImpl();
    CodecHelper.decodeTradeEvent(buf, res);

    System.out.println("Read Index::: " + buf.readerIndex());
    System.out.println("Write Index::: " + buf.writerIndex());

    System.out.println(res.getOpen());
    System.out.println(res.getHigh());
    System.out.println(res.getLow());
    System.out.println(res.getHigh());

    System.out.println(res.getOpenTime());
    System.out.println(res.getNumberOfTrades());

    System.out.println("Read Index>>>::: " + buf.readerIndex());
    System.out.println("Write Index>>>::: " + buf.writerIndex());
    System.out.println("Readable Size>>>::: " + buf.readableBytes());

    Assert.assertEquals(example.getSymbol(), res.getSymbol());
    Assert.assertEquals(example.getTimeKey(), res.getTimeKey());
    Assert.assertEquals(example.getOpenTime(), res.getOpenTime());

    Assert.assertEquals(example.getCloseTime(), res.getCloseTime());

    Assert.assertTrue(example.getOpen().equals(res.getOpen()));
    Assert.assertTrue(example.getHigh().equals(res.getHigh()));
    Assert.assertTrue(example.getLow().equals(res.getLow()));
    Assert.assertTrue(example.getClose().equals(res.getClose()));
  }
}
