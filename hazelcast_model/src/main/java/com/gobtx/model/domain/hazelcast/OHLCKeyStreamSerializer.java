package com.gobtx.model.domain.hazelcast;

import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.domain.OHLCKeyDataImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

import static com.gobtx.model.domain.hazelcast.HazelcastTypeIdContext.OHLC_KEY;
import static com.gobtx.model.domain.hazelcast.HazelcastTypeIdContext.US_ASCII;

/** Created by Aaron Kuai on 2019/11/14. */
public class OHLCKeyStreamSerializer implements StreamSerializer<OHLCKeyData> {

  @Override
  public void write(final ObjectDataOutput objectDataOutput, final OHLCKeyData ohlcKeyData)
      throws IOException {

    objectDataOutput.writeLong(ohlcKeyData.getTimeKey());
    final byte[] symbolChars = ohlcKeyData.getSymbol().getBytes(US_ASCII);
    objectDataOutput.writeInt(symbolChars.length);
    objectDataOutput.write(symbolChars);
  }

  @Override
  public OHLCKeyData read(ObjectDataInput objectDataInput) throws IOException {

    final OHLCKeyDataImpl res = new OHLCKeyDataImpl();

    res.setTimeKey(objectDataInput.readLong());

    final int len = objectDataInput.readInt();

    final byte[] valueBytes = new byte[len];
    objectDataInput.readFully(valueBytes);

    res.setSymbol(new String(valueBytes, US_ASCII));

    return res;
  }

  @Override
  public int getTypeId() {
    return OHLC_KEY;
  }

  @Override
  public void destroy() {}
}
