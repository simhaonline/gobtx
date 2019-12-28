package com.gobtx.model.enums;

import com.gobtx.model.utils.KLineKeyStrategy;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.concurrent.TimeUnit.*;

/** Created by Aaron Kuai on 2019/11/8. */
@SuppressWarnings("Duplicates")
public enum KlineInterval {
  m1(
      "1m",
      MINUTES.toMillis(1),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int min = calendar.get(calendar.MINUTE);
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          long minBase = year * 100000000L + month * 1000000L + day * 10000L + hour * 100L;

          return year * 100000000L + month * 1000000L + day * 10000L + hour * 100L + min;
        }
      },
      "minutely",
      DAYS.toMillis(5),
      MINUTES.toMillis(5),
      null),
  m3(
      "3m",
      MINUTES.toMillis(3),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int min = calendar.get(calendar.MINUTE);
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          long minBase = year * 100000000L + month * 1000000L + day * 10000L + hour * 100L;

          return year * 100000000L + month * 1000000L + day * 10000L + hour * 100L + (min / 3) * 3;
        }
      },
      "three_minute",
      DAYS.toMillis(15),
      MINUTES.toMillis(15),
      m1),
  m5(
      "5m",
      MINUTES.toMillis(5),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int min = calendar.get(calendar.MINUTE);
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          long minBase = year * 100000000L + month * 1000000L + day * 10000L + hour * 100L;

          return year * 100000000L + month * 1000000L + day * 10000L + hour * 100L + (min / 5) * 5;
        }
      },
      "five_minute",
      DAYS.toMillis(25),
      MINUTES.toMillis(25),
      m1),
  m15(
      "15m",
      MINUTES.toMillis(15),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int min = calendar.get(calendar.MINUTE);
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          long minBase = year * 100000000L + month * 1000000L + day * 10000L + hour * 100L;

          return year * 100000000L
              + month * 1000000L
              + day * 10000L
              + hour * 100L
              + (min / 15) * 15;
        }
      },
      "fifteen_minute",
      DAYS.toMillis(25),
      MINUTES.toMillis(75),
      m1),
  m30(
      "30m",
      MINUTES.toMillis(30),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int min = calendar.get(calendar.MINUTE);
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          long minBase = year * 100000000L + month * 1000000L + day * 10000L + hour * 100L;

          return year * 100000000L
              + month * 1000000L
              + day * 10000L
              + hour * 100L
              + (min / 30) * 30;
        }
      },
      "half_hour",
      DAYS.toMillis(30),
      MINUTES.toMillis(150),
      m1),

  h1(
      "1h",
      HOURS.toMillis(1),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 1000000L + month * 10000L + day * 100L + hour;
        }
      },
      "hourly",
      DAYS.toMillis(60),
      HOURS.toMillis(5),
      null),
  h2(
      "2h",
      HOURS.toMillis(2),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 1000000L + month * 10000L + day * 100L + (hour / 2) * 2;
        }
      },
      "two_hour",
      DAYS.toMillis(120),
      HOURS.toMillis(10),
      h1),
  h4(
      "4h",
      HOURS.toMillis(4),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 1000000L + month * 10000L + day * 100L + (hour / 4) * 4;
        }
      },
      "four_hour",
      DAYS.toMillis(240),
      HOURS.toMillis(20),
      h1),
  h6(
      "6h",
      HOURS.toMillis(6),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 1000000L + month * 10000L + day * 100L + (hour / 6) * 6;
        }
      },
      "six_hour",
      DAYS.toMillis(360),
      HOURS.toMillis(30),
      h1),
  h8(
      "8h",
      HOURS.toMillis(8),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 1000000L + month * 10000L + day * 100L + (hour / 8) * 8;
        }
      },
      "eight_hour",
      DAYS.toMillis(480),
      HOURS.toMillis(40),
      h1),
  h12(
      "12h",
      HOURS.toMillis(12),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int hour = calendar.get(calendar.HOUR_OF_DAY);
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 1000000L + month * 10000L + day * 100L + (hour / 12) * 12;
        }
      },
      "half_day",
      DAYS.toMillis(720),
      HOURS.toMillis(60),
      h1),

  d1(
      "1d",
      DAYS.toMillis(1),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 10000L + month * 100L + day;
        }
      },
      "daily",
      DAYS.toMillis(500),
      DAYS.toMillis(3),
      null),
  d3(
      "3d",
      DAYS.toMillis(3),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 10000L + month * 100L + day;
        }
      },
      "three_hour",
      DAYS.toMillis(1500),
      DAYS.toMillis(9),
      d1),

  w1(
      "1w",
      DAYS.toMillis(7),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 10000L + month * 100L + day;
        }
      },
      "weekly",
      DAYS.toMillis(3500),
      DAYS.toMillis(21),
      d1),

  M1(
      "1MT",
      DAYS.toMillis(30),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int day = calendar.get(calendar.DAY_OF_MONTH);

          return year * 100L + month;
        }
      },
      "monthly",
      DAYS.toMillis(1000),
      DAYS.toMillis(60),
      d1),
  Y1(
      "1Y",
      DAYS.toMillis(365),
      new KLineKeyStrategy() {
        @Override
        public long timeKey(Calendar calendar, long time) {
          calendar.setTimeInMillis(time);

          int year = calendar.get(Calendar.YEAR);
          return year;
        }
      },
      "yearly",
      DAYS.toMillis(36500),
      DAYS.toMillis(365 * 3),
      d1);

  private final String code;
  private final byte[] nameBytes;
  private final Long millis;
  private final KLineKeyStrategy kLineKeyStrategy;
  private final String tableSuffix;
  private final long historyLength;
  private final long overlapGap; // reload batch the overlap time gap
  private final KlineInterval parent;

  KlineInterval(
      final String code,
      final Long millis,
      final KLineKeyStrategy kLineKeyStrategy,
      final String tableSuffix,
      final long historyLength,
      final long overlapGap,
      final KlineInterval parent) {
    this.millis = millis;
    this.code = code;
    this.kLineKeyStrategy = kLineKeyStrategy;
    this.tableSuffix = tableSuffix;
    this.historyLength = historyLength;
    this.overlapGap = overlapGap;
    this.parent = parent;
    nameBytes = ("\"" + name() + "\"").getBytes(StandardCharsets.UTF_8);
  }

  public long getOverlapGap() {
    return overlapGap;
  }

  public KlineInterval getParent() {
    return parent;
  }

  public long getHistoryLength() {
    return historyLength;
  }

  public KLineKeyStrategy getkLineKeyStrategy() {
    return kLineKeyStrategy;
  }

  public Long getMillis() {
    return millis;
  }

  public String code() {
    return code;
  }

  public String getTableSuffix() {
    return tableSuffix;
  }

  /** Enumerated values. */
  public static final KlineInterval[] VALS = values();

  public static KlineInterval fromOrdinal(int ord) {
    return ord >= 0 && ord < VALS.length ? VALS[ord] : null;
  }

  public byte[] getNameBytes() {
    return nameBytes;
  }

  private static final Map<KlineInterval, LinkedHashSet<KlineInterval>> intervalMap;

  static {
    Map<KlineInterval, LinkedHashSet<KlineInterval>> temp = new HashMap<>();
    for (KlineInterval each : KlineInterval.VALS) {
      if (each.getParent() != null) {
        temp.computeIfAbsent(each.getParent(), interval -> new LinkedHashSet<>()).add(each);
      }
    }
    intervalMap = Collections.unmodifiableMap(temp);
  }

  public static Map<KlineInterval, LinkedHashSet<KlineInterval>> getIntervalMap() {
    return intervalMap;
  }
}
