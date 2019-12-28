package com.gobtx.hub.protocol;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.TradeEventData;
import com.gobtx.model.view.OHLCView;
import com.gobtx.model.view.TradeEventView;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Created by Aaron Kuai on 2019/11/18. */
public class CodecHelper {

  static Charset CHARSET = StandardCharsets.US_ASCII;

  public static TradeEventData decodeTradeEvent(final ByteBuf buf, final TradeEventData res) {
    final int symbolSize = buf.readInt();

    final byte[] symbolBytes = new byte[symbolSize];
    buf.readBytes(symbolBytes);
    res.setSymbol(new String(symbolBytes, CHARSET));

    res.setReportTimestamp(buf.readLong())
        .setTradeTimestamp(buf.readLong())
        .setPrice(readBigDecimal(buf))
        .setQuantity(readBigDecimal(buf))
        .setBuy(buf.readBoolean())
        .setBuyerMaker(buf.readBoolean());

    return res;
  }

  public static OHLCData decodeTradeEvent(final ByteBuf buf, final OHLCData res) {

    // 1. Symbol

    final int symbolSize = buf.readInt();

    final byte[] symbolBytes = new byte[symbolSize];
    buf.readBytes(symbolBytes);
    res.setSymbol(new String(symbolBytes, CHARSET));

    // res.setSymbol(buf.readBytes(symbolSize).toString(CHARSET));

    // 2. open time
    // 3. close time
    // 4. number of trade
    // 5. time key

    res.setOpenTime(buf.readLong())
        .setCloseTime(buf.readLong())
        .setNumberOfTrades(buf.readLong())
        .setTimeKey(buf.readLong());

    res.setOpen(readBigDecimal(buf))
        .setHigh(readBigDecimal(buf))
        .setLow(readBigDecimal(buf))
        .setClose(readBigDecimal(buf))
        .setAmount(readBigDecimal(buf))
        .setVolume(readBigDecimal(buf));

    return res;
  }

  public static int encodeTradeEvent(final ByteBuf buf, final TradeEventView data) {
    final int start = buf.writerIndex();

    buf.writerIndex(start + 4);
    // 1. Symbol:
    int size = writeString(buf, data.getSymbol(), CHARSET);

    buf.setInt(start, size);

    buf.writeLong(data.getReportTimestamp());
    buf.writeLong(data.getTradeTimestamp());

    writeBigDecimal(data.getPrice(), buf);
    writeBigDecimal(data.getQuantity(), buf);

    buf.writeBoolean(data.isBuy());
    buf.writeBoolean(data.isBuyerMaker());

    return buf.writerIndex() - start;
  }

  /**
   * First 8 byte is: <br>
   * 1. [0~3] the totally size of this package <br>
   * 2. [4~7] the size of the first <br>
   * symbol String value
   *
   * @param buf
   * @param data
   */
  public static int encodeTradeEvent(final ByteBuf buf, final OHLCView data) {

    final int start = buf.writerIndex();

    buf.writerIndex(start + 4);
    // 1. Symbol:
    int size = writeString(buf, data.getSymbol(), CHARSET);

    buf.setInt(start, size);

    // 2. open time
    // 3. close time
    // 4. number of trade
    // 5. time key
    buf.writeLong(data.getOpenTime())
        .writeLong(data.getCloseTime())
        .writeLong(data.getNumberOfTrades())
        .writeLong(data.getTimeKey());

    // This is the things

    // 6. open price
    writeBigDecimal(data.getOpen(), buf);
    // 7. high price
    writeBigDecimal(data.getHigh(), buf);
    // 8. low price
    writeBigDecimal(data.getLow(), buf);
    // 9. close price
    writeBigDecimal(data.getClose(), buf);

    // 10. the amount
    writeBigDecimal(data.getAmount(), buf);
    // 11. the volume
    writeBigDecimal(data.getVolume(), buf);

    return buf.writerIndex() - start;
  }

  protected static BigDecimal readBigDecimal(final ByteBuf buf) {

    int bSize = buf.readInt();
    final byte[] bytes = new byte[bSize];
    buf.readBytes(bytes);
    final BigInteger biValue = new BigInteger(bytes);
    return new BigDecimal(biValue, buf.readInt());
  }

  /**
   * * Just to write the big decimal to the stream
   *
   * @param value
   * @param dst
   * @return
   */
  protected static ByteBuf writeBigDecimal(final BigDecimal value, final ByteBuf dst) {

    final BigInteger biValue = value.unscaledValue();
    final byte[] valueBytes = biValue.toByteArray();

    dst.writeInt(valueBytes.length);
    dst.writeBytes(valueBytes);
    dst.writeInt(value.scale());

    return dst;
  }

  /**
   * @param dst
   * @param src
   * @param charset
   * @return
   */
  protected static int writeString(final ByteBuf dst, final String src, final Charset charset) {

    final byte[] bytes = src.getBytes(charset);
    dst.writeBytes(bytes);

    return bytes.length;
  }
}
