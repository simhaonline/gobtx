package com.gobtx.xchange.disruptor;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

/** Created by Aaron Kuai on 2019/11/11. */
public class HuobiMarketEventHandlerTest {

  static TimeZone shanghai = TimeZone.getTimeZone("Asia/Shanghai");

  @Test
  public void test4hourRule() {

    // 0/4/8/.....

    final long[] args = new long[] {1573444800l, 1573430400l, 1573401600l, 1573387200l};

    print(args);

    // {
    //      "amount": 7429.389020314446,
    //      "open": 9019.4,
    //      "close": 8772,
    //      "high": 9032.14,
    //      "id": 1573444800,
    //      "count": 62699,
    //      "low": 8738,
    //      "vol": 65979247.762956634
    //    },
    //    {
    //      "id": 1573430400,
    //      "open": 9034.13,
    //      "close": 9019.87,
    //      "high": 9071.42,
    //      "low": 9011.12,
    //      "vol": 27910657.68155554,
    //      "amount": 3086.617536807102,
    //      "count": 36011
    //    },
    //    {
    //      "id": 1573416000,
    //      "open": 9046.42,
    //      "close": 9034.13,
    //      "high": 9098.56,
    //      "low": 9020.56,
    //      "vol": 14646167.787483621,
    //      "amount": 1617.4421092631112,
    //      "count": 56126
    //    },
    //    {
    //      "id": 1573401600,
    //      "open": 8796.19,
    //      "close": 9045.7,
    //      "high": 9139,
    //      "low": 8785.6,
    //      "vol": 98101752.29868913,
    //      "amount": 10883.960353354985,
    //      "count": 92904
    //    },
    //    {
    //      "id": 1573387200,
    //      "open": 8830.41,
    //      "close": 8796.92,
    //      "high": 8860.02,
    //      "low": 8782.13,
    //      "vol": 26351007.574918885,
    //      "amount": 2988.311569407873,
    //      "count": 42133
    //    }

  }

  private void print(final long[] args) {
    Calendar calendar = Calendar.getInstance(shanghai);
    for (final long it : args) {

      System.out.println("\n\n====================================\n\n");
      calendar.setTimeInMillis(it * 1000);

      System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
      System.out.println("MONTH: " + (calendar.get(Calendar.MONTH) + 1));
      System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));

      System.out.println("Calendar.DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));

      System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
      System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
    }
  }

  @Test
  public void testHuobiWeekRule() {

    // Begin from the Sunday of Beijing, SUN day is the first things
    final long[] args =
        new long[] {
          1573315200l, 1572710400L, 1572105600L, 1571500800L, 1570896000L,
        };

    print(args);

    // {
    //      "amount": 37611.65080270497,
    //      "open": 8801.26,
    //      "close": 8776.99,
    //      "high": 9139,
    //      "id": 1573315200,
    //      "count": 466367,
    //      "low": 8728,
    //      "vol": 335263503.6928716
    //    },
    //    {
    //      "id": 1572710400,
    //      "open": 9319.84,
    //      "close": 8800.01,
    //      "high": 9500,
    //      "low": 8691.29,
    //      "vol": 1804264160.786051,
    //      "amount": 196625.25462994564,
    //      "count": 1905514
    //    },
    //    {
    //      "id": 1572105600,
    //      "open": 9194.08,
    //      "close": 9319.84,
    //      "high": 9897.12,
    //      "low": 8900,
    //      "vol": 2969403620.791557,
    //      "amount": 319310.29948031413,
    //      "count": 2986677
    //    },
    //    {
    //      "id": 1571500800,
    //      "open": 7990.74,
    //      "close": 9194.08,
    //      "high": 10350,
    //      "low": 7300,
    //      "vol": 2967642439.20079,
    //      "amount": 349690.13832410576,
    //      "count": 3137961
    //    },
    //    {
    //      "id": 1570896000,
    //      "open": 8371.9,
    //      "close": 7990.74,
    //      "high": 8449.3,
    //      "low": 7820,
    //      "vol": 1168449791.1955156,
    //      "amount": 143667.33123649744,
    //      "count": 1644106
    //    }
  }

  @Test
  public void testHuobi5minKeyRule() {
    // In seconds not in ms

    // {
    //  "status": "ok",
    //  "ch": "market.btcusdt.kline.5min",
    //  "ts": 1573455377163,
    //  "data": [
    //    {
    //      "amount": 63.56938882647757,
    //      "open": 8768.98,
    //      "close": 8771.02,
    //      "high": 8782.98,
    //      "id": 1573455300,
    //      "count": 616,
    //      "low": 8768.37,
    //      "vol": 557754.1418695232
    //    }
    //  ]
    // }

    Calendar calendar = Calendar.getInstance(shanghai);

    calendar.setTimeInMillis(1573455300L * 1000);

    System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));

    System.out.println("SECOND: " + calendar.get(Calendar.SECOND));

    // 55 - 50  this is in seconds ID

    calendar.setTimeInMillis(1573455000l * 1000);

    System.out.println("MINUTE2: " + calendar.get(Calendar.MINUTE));

    System.out.println("SECOND2: " + calendar.get(Calendar.SECOND));
  }
  /**
   * Huobi protocol
   *
   * <p>1min, 5min, 15min, 30min, 60min, 4hour, 1day, 1mon, 1week, 1year
   *
   * <p>https://api.huobi.pro/market/history/kline?period=1min&size=5&symbol=btcusdt
   * https://api.huobi.pro/market/history/kline?period=60min&size=5&symbol=btcusdt
   * https://api.huobi.pro/market/history/kline?period=1day&size=5&symbol=btcusdt
   * https://api.huobi.pro/market/history/kline?period=1mon&size=5&symbol=btcusdt
   *
   * <p>https://api.huobi.pro/market/history/kline?period=5min&size=5&symbol=btcusdt
   * https://api.huobi.pro/market/history/kline?period=15min&size=5&symbol=btcusdt
   * https://api.huobi.pro/market/history/kline?period=4hour&size=5&symbol=btcusdt
   * https://api.huobi.pro/market/history/kline?period=1week&size=5&symbol=btcusdt
   * https://api.huobi.pro/market/history/kline?period=1year&size=5&symbol=btcusdt
   */
  @Test
  public void testHuobi1minKeyRule() {

    // {
    //  "status": "ok",
    //  "ch": "market.btcusdt.kline.1min",
    //  "ts": 1573454026112,
    //  "data": [
    //    {
    //      "amount": 10.113874833143116,
    //      "open": 8835.09,
    //      "close": 8840.81,
    //      "high": 8842.85,
    //      "id": 1573453980,
    //      "count": 237,
    //      "low": 8833.2,
    //      "vol": 89388.94353037
    //    }
    // ]
    // }

    // 201911111433  suppose be the key

    Calendar calendar = Calendar.getInstance(shanghai);

    calendar.setTimeInMillis(1573453980L * 1000);

    System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
    System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
    System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));

    System.out.println("Calendar.DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));

    System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));

    System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));

    System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
    System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
  }
}
