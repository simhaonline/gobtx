package com.gobtx.hub.netty;

import com.gobtx.model.domain.OHLCDataImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/** Created by Aaron Kuai on 2019/11/18. */
public class HubMessageDecoderTest {

  @Test
  public void testProtoStuff() {

    //    final RuntimeSchema<OHLCDataImpl> schema = RuntimeSchema.createFrom(OHLCDataImpl.class);
    //
    //    (schema)
    //        .getFields()
    //        .forEach(
    //            it -> {
    //              System.out.println(it.name);
    //              System.out.println(it.type);
    //              System.out.println(it.repeated);
    //              System.out.println(it.getClass().getName());
    //              System.out.println("-----------------");
    //            });
    //
    //    OHLCDataImpl example =
    //        new OHLCDataImpl()
    //            .setSymbol("usdjpy")
    //            .setOpenTime(System.currentTimeMillis())
    //            .setCloseTime(System.currentTimeMillis())
    //            .setOpen(BigDecimal.valueOf(23131.2313d))
    //            .setHigh(BigDecimal.valueOf(23131.2313d))
    //            .setLow(BigDecimal.valueOf(23131.2313d))
    //            .setClose(BigDecimal.valueOf(23131.2313d))
    //            .setAmount(BigDecimal.valueOf(23131313l))
    //            .setVolume(BigDecimal.valueOf(223028d))
    //            .setNumberOfTrades(System.currentTimeMillis());
    //
    //    LinkedBuffer buffer = LinkedBuffer.allocate(256);
    //
    //    final byte[] protostuff;
    //    try {
    //      protostuff = ProtobufIOUtil.toByteArray(example, schema, buffer);
    //    } finally {
    //      buffer.clear();
    //    }
    //
    //    OHLCDataImpl fooParsed = schema.newMessage();
    //    ProtobufIOUtil.mergeFrom(protostuff, fooParsed, schema);
    //
    //    System.out.println(fooParsed.getSymbol());
    //    System.out.println(fooParsed.getOpen());
    //
    //    System.out.println(protostuff.length);
  }

  protected ByteBuf writeString(final ByteBuf dst, final CharBuffer src, final Charset charset) {

    final CharsetEncoder encoder = CharsetUtil.encoder(charset);
    int length = (int) ((double) src.remaining() * encoder.maxBytesPerChar());
    boolean release = true;

    try {
      final ByteBuffer dstBuf = dst.internalNioBuffer(dst.readerIndex(), length);
      final int pos = dstBuf.position();
      CoderResult cr = encoder.encode(src, dstBuf, true);
      if (!cr.isUnderflow()) {
        cr.throwException();
      }
      cr = encoder.flush(dstBuf);
      if (!cr.isUnderflow()) {
        cr.throwException();
      }
      dst.writerIndex(dst.writerIndex() + dstBuf.position() - pos);
      release = false;
      return dst;
    } catch (CharacterCodingException x) {
      throw new IllegalStateException(x);
    } finally {
      if (release) {
        dst.release();
      }
    }
  }

  protected int writeBigDecimal(final BigDecimal value, final ByteBuf dst) {

    int startIndex = dst.writerIndex();
    final BigInteger biValue = value.unscaledValue();
    final byte[] valueBytes = biValue.toByteArray();

    dst.writeInt(valueBytes.length);
    dst.writeBytes(valueBytes);
    dst.writeInt(value.scale());

    return dst.writerIndex() - startIndex;
  }

  @Test
  public void testPureNettySolutions() {

    OHLCDataImpl example =
        new OHLCDataImpl()
            .setSymbol("USDJPY")
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

    int start = buf.writerIndex();
    buf.writerIndex(start + 8);
    // 1. Symbol:
    writeString(buf, CharBuffer.wrap(example.getSymbol()), StandardCharsets.US_ASCII);

    buf.setInt(start + 4, buf.writerIndex() - start - 4);

    // 2. open time
    // 3. close time
    // 4. number of trade
    buf.writeLong(example.getOpenTime())
        .writeLong(example.getCloseTime())
        .writeLong(example.getNumberOfTrades());

    // This is the things

    // 5. open price
    writeBigDecimal(example.getOpen(), buf);
    // 7. high price
    writeBigDecimal(example.getHigh(), buf);
    // 8. low price
    writeBigDecimal(example.getLow(), buf);
    // 9. close price
    writeBigDecimal(example.getClose(), buf);

    buf.setInt(start, buf.writerIndex() - start); // The entire size

    System.out.println("SIZE::: " + (buf.writerIndex() - start)); // 86  VS   116

    System.out.println("AVG:: CHANGE " + (86D / 116D)); // ~74%
  }

  @Test
  public void testAvgSizeOfTheTickerSize() throws IOException {

    //    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
    //    InputStream stream =
    //
    // HubMessageDecoderTest.class.getClassLoader().getResourceAsStream("sample/binance_m1.log");
    //
    //    Reader reader = new InputStreamReader(stream, decoder);
    //
    //    List<OHLCDataImpl> datas = new ArrayList<>();
    //
    //    try (BufferedReader breader = new BufferedReader(reader)) {
    //
    //      for (; ; ) {
    //        String line = breader.readLine();
    //        if (line == null) break;
    //
    //        final String[] row = line.split("[|]");
    //
    //        final String symbol = row[1];
    //        final KlineInterval klineInterval = KlineInterval.valueOf(row[2]);
    //        final long openTime = Long.parseLong(row[3]);
    //        final long closeTime = Long.parseLong(row[4]);
    //
    //        final BigDecimal open = new BigDecimal(row[5]);
    //        final BigDecimal high = new BigDecimal(row[6]);
    //        final BigDecimal low = new BigDecimal(row[7]);
    //        final BigDecimal close = new BigDecimal(row[8]);
    //        final BigDecimal volume = new BigDecimal(row[10]);
    //        final BigDecimal amount = new BigDecimal(row[11]);
    //        final long numberOfTrades = Long.parseLong(row[12]);
    //
    //        OHLCDataImpl data = new OHLCDataImpl();
    //        data.setSymbol(symbol)
    //            .setOpenTime(openTime)
    //            .setOpen(open)
    //            .setHigh(high)
    //            .setLow(low)
    //            .setClose(close)
    //            .setCloseTime(closeTime)
    //            .setVolume(volume)
    //            .setAmount(amount)
    //            .setNumberOfTrades(numberOfTrades);
    //
    //        datas.add(data);
    //      }
    //    }
    //
    //    final RuntimeSchema<OHLCDataImpl> schema = RuntimeSchema.createFrom(OHLCDataImpl.class);
    //
    //    // io.protostuff.runtime.RuntimeSchema
    //
    //    System.out.println(schema.getClass().getName());
    //
    //    int max = 0, min = Integer.MAX_VALUE;
    //
    //    int cnt = datas.size();
    //    int sum = 0;
    //
    //    for (final OHLCDataImpl data : datas) {
    //
    //      LinkedBuffer buffer = LinkedBuffer.allocate(256);
    //
    //      final byte[] protostuff;
    //      try {
    //        protostuff = ProtobufIOUtil.toByteArray(data, schema, buffer);
    //      } finally {
    //        buffer.clear();
    //      }
    //
    //      int size = protostuff.length;
    //
    //      // System.out.println("SIZE::: " + size);
    //
    //      if (max < size) max = size;
    //      if (min > size) min = size;
    //
    //      sum += size;
    //    }
    //
    //    System.out.println("MAX SIZE: " + max);
    //    System.out.println("MIN SIZE: " + min);
    //    System.out.println("AVG SIZE: " + sum / cnt);
  }
}
