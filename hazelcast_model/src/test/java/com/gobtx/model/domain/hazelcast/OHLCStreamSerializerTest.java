package com.gobtx.model.domain.hazelcast;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/** Created by Aaron Kuai on 2019/11/12. */
public class OHLCStreamSerializerTest {

  static int bitLengthForInt(int n) {
    return 32 - Integer.numberOfLeadingZeros(n);
  }

  public static byte[] rawLongArray(long val) {

    int highWord = (int) (val >>> 32);
    final boolean big = highWord > 0;
    int lowWord = (int) val;

    if (big) {

      int highSize = bitLengthForInt(highWord);

      // the low word 4 byte is full so
      final byte[] byteArray = new byte[4 + highSize];

      // Write the high firstly
      // Big Endian

      int i = 4 + highSize - 1;

      while (highSize > 0) {
        byteArray[i--] = (byte) highWord;
        highWord >>>= 8;
        highSize--;
      }

      byteArray[3] = (byte) lowWord;
      lowWord >>>= 8;
      byteArray[2] = (byte) lowWord;
      lowWord >>>= 8;
      byteArray[1] = (byte) lowWord;
      lowWord >>>= 8;
      byteArray[0] = (byte) lowWord;

      return byteArray;
    } else {
      // Small one
      int bitSize = bitLengthForInt(lowWord);

      final int byteLen = bitSize / 8 + 1;
      final byte[] byteArray = new byte[byteLen];

      int i = byteLen - 1;

      while (i >= 0) {

        byteArray[i] = (byte) lowWord;
        lowWord >>>= 8;
        i--;
      }

      return byteArray;
    }
  }

  public static boolean compareBytes(final byte[] left, final byte[] right) {

    if (left.length != right.length) return false;

    for (int i = 0; i < left.length; i++) {
      if (left[i] != right[i]) {
        return false;
      }
    }

    return true;
  }

  @Test
  public void testBigInteger() {

    long smallLong = 10l;
    long bigLong = 3213121231l;

    BigInteger bigInteger = BigInteger.valueOf(bigLong);

    byte[] bytes = bigInteger.toByteArray();

    printBytes(bytes);

    System.out.println("\n==========\n");

    byte[] rawBytes = rawLongArray(bigLong);

    printBytes(rawBytes);

    Assert.assertTrue(compareBytes(bytes, rawBytes));

    System.out.println("\nXXXXXXXXXXXXXXXXXXXXXXXXX\n");

    BigInteger smallInteger = BigInteger.valueOf(smallLong);

    bytes = smallInteger.toByteArray();

    System.out.println("\n==========\n");

    printBytes(bytes);

    rawBytes = rawLongArray(smallLong);

    printBytes(rawBytes);

    Assert.assertTrue(compareBytes(bytes, rawBytes));
  }

  public static void printBytes(final byte[] bytes) {

    final StringBuffer sb = new StringBuffer();

    sb.append("Size: ").append(bytes.length).append("\n");

    for (final byte each : bytes) {
      sb.append("\n\t").append(each);
    }

    System.out.println(sb.toString());
  }
}
