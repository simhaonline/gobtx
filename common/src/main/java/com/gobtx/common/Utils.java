package com.gobtx.common;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Set;

public abstract class Utils {

  //    @FunctionalInterface
  //    public interface CharArrayGetter {
  //        char[] value(final String str);
  //    }
  //
  //    static final CharArrayGetter GETTER;
  //
  //    static {
  //
  //        CharArrayGetter DEF = str -> str.toCharArray();
  //
  //        CharArrayGetter assign;
  //        try {
  //
  //            Field valueField = String.class.getDeclaredField("value");
  //            valueField.setAccessible(true);
  //
  //            assign = str -> {
  //                try {
  //                    return (char[]) valueField.get(str);
  //                } catch (Exception e) {
  //                    return str.toCharArray();
  //                }
  //            };
  //
  //        } catch (Throwable throwable) {
  //            assign = DEF;
  //        }
  //
  //        GETTER = assign;
  //
  //    }

  // 48: 0
  // 49: 1
  // 50: 2
  // 51: 3
  // 52: 4
  // 53: 5
  // 54: 6
  // 55: 7
  // 56: 8
  // 57: 9

  // 65: A
  // 66: B
  // 67: C
  // 68: D
  // 69: E
  // 70: F

  // 97: a
  // 98: b
  // 99: c
  // 100: d
  // 101: e
  // 102: f

  static final int digits[] = {
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 9
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 19
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 29
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 39
    0, 0, 0, 0, 0, 0, 0, 0, // 47
    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, // 57
    0, 0, 0, 0, 0, 0, 0, // 64
    10, 11, 12, 13, 14, 15, // 70
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 80
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 90
    0, 0, 0, 0, 0, 0, // 96
    10, 11, 12, 13, 14, 15
  };

  static final char[] digits2 = {
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'A', 'B',
    'C', 'D', 'E', 'F'
  };

  static int hexRadix = 1 << 4;
  static int hexMask = hexRadix - 1;

  @FunctionalInterface
  public interface StringInitiator {
    String build(final char[] buf);
  }

  public static void closeQuietly(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Throwable throwable) {

      }
    }
  }

  static final StringInitiator STRING_INITIATOR;

  static {
    StringInitiator def = buf -> new String(buf);

    try {
      final Constructor<String> constructor =
          String.class.getDeclaredConstructor(char[].class, boolean.class);
      constructor.setAccessible(true);
      def =
          buf -> {
            try {
              /** This to avoid copy of the under line array */
              return constructor.newInstance(buf, true);
            } catch (Throwable e) {
              // TODO: 2019/3/13 move to logger
              System.err.println("Shared values model failed in runtime");
              return new String(buf);
            }
          };
    } catch (Throwable throwable) {
      // TODO: 2019/3/13 move to logger
      System.err.println("Can not use the shared values model in String");
    }
    STRING_INITIATOR = def;
  }

  static final char ZERO = '0';

  /**
   * Optimized for quick converter the long to a fix length(16) hex upper case string
   *
   * @param val
   * @return
   */
  public static String toHexStringZeroPrefix(long val) {

    char[] buf = new char[16];
    int charPos = 15;
    do {
      buf[charPos--] = digits2[((int) val) & hexMask];
      val >>= 4;
    } while (val != 0 && charPos > 0);

    while (charPos >= 0) {
      buf[charPos--] = ZERO;
    }
    return STRING_INITIATOR.build(buf);
  }

  public static String toHexStringZeroPrefix(long val, int size) {

    char[] buf = new char[size];
    int charPos = size - 1;
    do {
      buf[charPos--] = digits2[((int) val) & hexMask];
      val >>= 4;
    } while (val != 0); // && charPos > 0);

    while (charPos >= 0) {
      buf[charPos--] = ZERO;
    }
    return STRING_INITIATOR.build(buf);
  }

  //    public static void main(String[] args) {
  //        int hashcode = "EURUSD".hashCode();
  //        System.out.println(Integer.toHexString(hashcode));
  //        System.out.println(Utils.toHexStringZeroPrefix(hashcode, 5));
  //    }

  /**
   * 1. Can not parse negative etc 2. Must be validated firstly no rules check 3. When in small
   * volume this speed can not beat the JDK!!!! 4. JDK did some trick thing to optimize this in
   * native
   *
   * <p>Normally DO NOT USE THIS!
   *
   * @param values
   * @param start
   * @param end
   * @return
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  @Deprecated
  public static long fastHex2Long(final String values, int start, final int end) {

    long res = 0;
    while (start < end) {
      res = res << 4 | digits[values.charAt(start++)];
    }

    return res;
  }

  /**
   * @param values
   * @param start
   * @param end
   * @return
   */
  @Deprecated
  public static long fastHex2Long2(final String values, int start, int end) {

    long result = 0;
    int digit;
    while (start < end) {
      // Accumulating negatively avoids surprises near MAX_VALUE
      digit = Character.digit(values.charAt(start++), 16);
      result *= 16;
      result -= digit;
    }
    return result;
  }

  /**
   * Fast method of finding the next power of 2 greater than or equal to the supplied value.
   *
   * <p>If the value is {@code <= 0} then 1 will be returned. This method is not suitable for {@link
   * Integer#MIN_VALUE} or numbers greater than 2^30.
   *
   * @param value from which to search for next power of 2
   * @return The next power of 2 or the value itself if it is a power of 2
   */
  public static int findNextPositivePowerOfTwo(final int value) {
    assert value > Integer.MIN_VALUE && value < 0x40000000;
    return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
  }

  /**
   * Fast method of finding the next power of 2 greater than or equal to the supplied value.
   *
   * <p>This method will do runtime bounds checking and call {@link
   * #findNextPositivePowerOfTwo(int)} if within a valid range.
   *
   * @param value from which to search for next power of 2
   * @return The next power of 2 or the value itself if it is a power of 2.
   *     <p>Special cases for return values are as follows:
   *     <ul>
   *       <li>{@code <= 0} -> 1
   *       <li>{@code >= 2^30} -> 2^30
   *     </ul>
   */
  public static int safeFindNextPositivePowerOfTwo(final int value) {
    return value <= 0 ? 1 : value >= 0x40000000 ? 0x40000000 : findNextPositivePowerOfTwo(value);
  }

  public static int hashCode(Object o) {
    int h = Objects.hashCode(o);
    if (h != 0) {
      h = (h & 0xFF) + ((h & 0xFF) << 8) + ((h & 0xFF) << 16) + ((h & 0xFF) << 24);
    }
    return h;
  }

  public static final String OS_NAME = getSystemProperty("os.name");

  public static final boolean IS_OS_LINUX = getOsMatchesName("Linux") || getOsMatchesName("LINUX");

  private static boolean getOsMatchesName(final String osNamePrefix) {
    return isOSNameMatch(OS_NAME, osNamePrefix);
  }

  static boolean isOSNameMatch(final String osName, final String osNamePrefix) {
    if (osName == null) {
      return false;
    }
    return osName.startsWith(osNamePrefix);
  }

  public static String sysProperty(final String key, final String def) {

    String value = System.getenv(key);
    if (value == null || value.trim().isEmpty()) {
      value = System.getProperty(key);
    }
    return (value == null || value.trim().isEmpty()) ? def : value.trim();
  }

  private static String getSystemProperty(final String property) {
    try {
      return System.getProperty(property);
    } catch (final SecurityException ex) {
      // we are not allowed to look at this property
      // System.err.println("Caught a SecurityException reading the system property '" + property
      // + "'; the SystemUtils property value will default to null.");
      return null;
    }
  }

  public static boolean equalSets(final Set set1, final Set set2) {

    if (set1 == null && set2 == null) return true;

    if (set1 == null && set2 != null) return false;

    if (set2 == null && set1 != null) return false;

    if (set1.size() != set2.size()) return false;

    for (Object obj1 : set1) {
      if (!set2.contains(obj1)) {
        return false;
      }
    }
    return true;
  }

  public static boolean checkMinMemory(long min) {
    long maxMem = Runtime.getRuntime().maxMemory();

    if (maxMem < .85 * min) {
      return false;
    }
    return true;
  }

  public static final String RPC_HOST_ENV = "rpc.host";
  public static final String RPC_PORT_ENV = "rpc.port";
  public static final String GRPC_PORT_ENV = "grpc.port";
  public static final String WEB_SOCKET_PORT_ENV = "ws.port";

  public static int getRpcPort(int given) {
    return getPort(RPC_PORT_ENV, given == 0 ? 8088 : given);
  }

  public static int getGrpcPort(int given) {
    return getPort(GRPC_PORT_ENV, given == 0 ? 8087 : given);
  }

  public static int getWebSocketPort(int given) {
    return getPort(WEB_SOCKET_PORT_ENV, given == 0 ? 8089 : given);
  }

  public static String getRpcHost(String host) {
    return Utils.sysProperty(RPC_HOST_ENV, host);
  }

  public static int getPort(final String env, final int given) {

    String envPort = Utils.sysProperty(env, "NA");

    System.out.println("POP_UP_ENV_PORT " + env + " " + envPort + "  default  " + given);

    if (!envPort.isEmpty() && !"NA".equalsIgnoreCase(envPort) && !"0".equals(envPort)) {

      return Integer.parseInt(envPort);
    }
    return given;
  }
}
