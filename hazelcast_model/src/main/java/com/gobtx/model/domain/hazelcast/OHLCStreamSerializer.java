package com.gobtx.model.domain.hazelcast;

import com.gobtx.common.NumberBytesCodec;
import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.gobtx.model.domain.hazelcast.HazelcastTypeIdContext.US_ASCII;

/**
 * @Refer
 * http://docs.cascading.org/cascading/2.5/cascading-hadoop/src-html/cascading/tuple/hadoop/BigDecimalSerialization.html
 *
 * <p>Created by Aaron Kuai on 2019/11/12.
 */
public class OHLCStreamSerializer implements StreamSerializer<OHLCData> {

  @FunctionalInterface
  interface BigDecimalSerializer {
    void serializer(final BigDecimal bigDecimal, final ObjectDataOutput objectDataOutput)
        throws IOException;
  }

  static final BigDecimalSerializer bigDecimalSerializer;

  static {
    BigDecimalSerializer res;
    try {

      final Field intVal = BigDecimal.class.getDeclaredField("intVal"); // BigInteger
      intVal.setAccessible(true);
      final Field intCompact = BigDecimal.class.getDeclaredField("intCompact"); // long
      intCompact.setAccessible(true);

      res =
          (bigDecimal, objectDataOutput) -> {
            try {

              final BigInteger bi = (BigInteger) intVal.get(bigDecimal);

              if (bi == null) {
                final long intCompactValue = (long) intCompact.get(bigDecimal);

                final byte[] valueBytes = NumberBytesCodec.rawLongArray(intCompactValue);

                objectDataOutput.writeInt(valueBytes.length);
                objectDataOutput.write(valueBytes);

              } else {

                final byte[] valueBytes = bi.toByteArray();

                objectDataOutput.writeInt(valueBytes.length);
                objectDataOutput.write(valueBytes);
              }
              objectDataOutput.writeInt(bigDecimal.scale());

            } catch (IllegalAccessException e) {
              jdkBigDecimalSerializer(bigDecimal, objectDataOutput);
            }
          };

    } catch (Throwable throwable) {
      res = OHLCStreamSerializer::jdkBigDecimalSerializer;
    }
    bigDecimalSerializer = res;
  }
  // Long

  private static void jdkBigDecimalSerializer(
      final BigDecimal value, final ObjectDataOutput objectDataOutput) throws IOException {

    final BigInteger biValue = value.unscaledValue();
    final byte[] valueBytes = biValue.toByteArray();

    objectDataOutput.writeInt(valueBytes.length);
    objectDataOutput.write(valueBytes);
    objectDataOutput.writeInt(value.scale());
  }

  @Override
  public void write(ObjectDataOutput objectDataOutput, OHLCData ohlcData) throws IOException {

    final byte[] symbolChars = ohlcData.getSymbol().getBytes(US_ASCII);
    objectDataOutput.writeInt(symbolChars.length);
    objectDataOutput.write(symbolChars);

    objectDataOutput.writeLong(ohlcData.getTimeKey());
    objectDataOutput.writeLong(ohlcData.getOpenTime());
    objectDataOutput.writeLong(ohlcData.getCloseTime());

    // Market data
    // 1. byte size
    // 2. bytes
    // 3.  scale
    bigDecimalSerializer.serializer(ohlcData.getOpen(), objectDataOutput);
    bigDecimalSerializer.serializer(ohlcData.getHigh(), objectDataOutput);
    bigDecimalSerializer.serializer(ohlcData.getLow(), objectDataOutput);
    bigDecimalSerializer.serializer(ohlcData.getClose(), objectDataOutput);

    // Market statistic
    bigDecimalSerializer.serializer(ohlcData.getVolume(), objectDataOutput);
    bigDecimalSerializer.serializer(ohlcData.getAmount(), objectDataOutput);
    objectDataOutput.writeLong(ohlcData.getNumberOfTrades());
  }

  private BigDecimal readBigDecimal(ObjectDataInput objectDataInput) throws IOException {

    final int len = objectDataInput.readInt();
    final byte[] valueBytes = new byte[len];
    objectDataInput.readFully(valueBytes);
    BigInteger value = new BigInteger(valueBytes);
    return new BigDecimal(new BigInteger(valueBytes), objectDataInput.readInt());
  }

  @Override
  public OHLCData read(ObjectDataInput objectDataInput) throws IOException {

    OHLCDataImpl res = new OHLCDataImpl();

    final int len = objectDataInput.readInt();
    final byte[] valueBytes = new byte[len];
    objectDataInput.readFully(valueBytes);
    res.setSymbol(new String(valueBytes, US_ASCII));

    res.setTimeKey(objectDataInput.readLong());
    res.setOpenTime(objectDataInput.readLong());
    res.setCloseTime(objectDataInput.readLong());

    res.setOpen(readBigDecimal(objectDataInput));
    res.setHigh(readBigDecimal(objectDataInput));
    res.setLow(readBigDecimal(objectDataInput));
    res.setClose(readBigDecimal(objectDataInput));

    res.setVolume(readBigDecimal(objectDataInput));
    res.setAmount(readBigDecimal(objectDataInput));
    res.setNumberOfTrades(objectDataInput.readLong());

    return res;
  }

  @Override
  public int getTypeId() {
    return HazelcastTypeIdContext.OHLC;
  }

  @Override
  public void destroy() {}
}
