package com.gobtx.common;

import java.math.BigInteger;

/**
 * Always in Big Endian
 *
 * @see java.nio.Bits
 * @see BigInteger#toByteArray()
 */
public abstract class NumberBytesCodec {

  static int bitLengthForInt(int n) {
    return 32 - Integer.numberOfLeadingZeros(n);
  }

  /**
   * This is to assume it is Positive value not negative
   *
   * @see BigInteger#toByteArray()
   * @param val
   * @return
   */
  public static byte[] rawLongArray(final long val) {

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

  private static byte int3(int x) {
    return (byte) (x >> 24);
  }

  private static byte int2(int x) {
    return (byte) (x >> 16);
  }

  private static byte int1(int x) {
    return (byte) (x >> 8);
  }

  private static byte int0(int x) {
    return (byte) (x);
  }

  public static byte[] toByte(final int value) {

    byte[] res = new byte[4];

    res[0] = int3(value);
    res[1] = int2(value);
    res[2] = int1(value);
    res[3] = int0(value);

    return res;
  }

  private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
    return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
  }

  public static boolean validateIntBytes(final byte[] value) {
    return value != null && value.length >= 4;
  }

  public static int toInt(final byte[] value) {
    return makeInt(value[0], value[1], value[2], value[3]);
  }

  // ----------Long---------------------------

  private static byte long7(long x) {
    return (byte) (x >> 56);
  }

  private static byte long6(long x) {
    return (byte) (x >> 48);
  }

  private static byte long5(long x) {
    return (byte) (x >> 40);
  }

  private static byte long4(long x) {
    return (byte) (x >> 32);
  }

  private static byte long3(long x) {
    return (byte) (x >> 24);
  }

  private static byte long2(long x) {
    return (byte) (x >> 16);
  }

  private static byte long1(long x) {
    return (byte) (x >> 8);
  }

  private static byte long0(long x) {
    return (byte) (x);
  }

  public static byte[] toByte(final long value) {

    byte[] res = new byte[8];
    res[0] = long7(value);
    res[1] = long6(value);
    res[2] = long5(value);
    res[3] = long4(value);
    res[4] = long3(value);
    res[5] = long2(value);
    res[6] = long1(value);
    res[7] = long0(value);

    return res;
  }

  private static long makeLong(
      byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
    return ((((long) b7) << 56)
        | (((long) b6 & 0xff) << 48)
        | (((long) b5 & 0xff) << 40)
        | (((long) b4 & 0xff) << 32)
        | (((long) b3 & 0xff) << 24)
        | (((long) b2 & 0xff) << 16)
        | (((long) b1 & 0xff) << 8)
        | (((long) b0 & 0xff)));
  }

  public static boolean validateLongBytes(final byte[] value) {
    return value != null && value.length >= 8;
  }

  public static long toLong(final byte[] value) {

    return makeLong(value[0], value[1], value[2], value[3], value[4], value[5], value[6], value[7]);
  }
}
