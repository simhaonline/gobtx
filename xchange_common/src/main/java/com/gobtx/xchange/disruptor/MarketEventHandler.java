package com.gobtx.xchange.disruptor;

import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.enums.KlineIntervalGroup;
import com.gobtx.model.view.OHLCView;
import com.gobtx.xchange.aggregator.PostAggregateListener;
import com.gobtx.xchange.configuration.ExchangeIntervalMapper;
import com.gobtx.xchange.repository.MarketDataRepository;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Created by Aaron Kuai on 2019/11/11. */
@SuppressWarnings("Duplicates")
public class MarketEventHandler implements EventHandler<MarketEvent>, LifecycleAware {

  protected static final Logger logger = LoggerFactory.getLogger(MarketEventHandler.class);

  protected final MarketDataRepository repository;

  protected final int segmentId;

  protected final List<PostAggregateListener> listeners;

  public MarketEventHandler(
      final MarketDataRepository repository,
      final int segmentId,
      final List<PostAggregateListener> listeners) {

    this.repository = repository;
    this.segmentId = segmentId;
    this.listeners = listeners;
  }

  @Override
  public void onEvent(final MarketEvent marketEvent, final long sequence, final boolean endOfBatch)
      throws Exception {

    if (marketEvent.isDeadEvent()) {
      // Manually trigger flush
      triggerCheckPoint(true);
      logger.warn("DISRUPTOR_HANDLER_DEAD_TRIGGER {}", segmentId);
    }

    if (marketEvent.isCheckPointEvent()) {
      triggerCheckPoint(false);
    }

    if (marketEvent.segmentId == segmentId) {

      switch (marketEvent.type) {
        case m1:
          tryAggregateOneMinute(marketEvent);
          break;
        case h1:
          tryAggregateOneHour(marketEvent);
          break;
        case d1:
          tryAggregateOneDay(marketEvent);
          break;
        default:
          final OHLCView data =
              repository.update(
                  marketEvent.exchange,
                  marketEvent.symbol,
                  marketEvent.type,
                  marketEvent
                      .type
                      .getkLineKeyStrategy()
                      .timeKey(marketEvent.calendar, marketEvent.data.getOpenTime()),
                  marketEvent.data);

          for (final PostAggregateListener listener : listeners) {
            listener.one(
                marketEvent.exchange,
                marketEvent.symbol,
                marketEvent.type,
                data,
                marketEvent.isDerived());
          }

          // this is to recon so over write the things
          break;
      }
    }
  }

  private void tryAggregateOneMinute(final MarketEvent marketEvent) {

    // Aggregate the
    // m3
    // m5
    // m15
    // m30
    //    m1("1m", MINUTES.toMillis(1))
    //    m3("3m", MINUTES.toMillis(3))
    //    m5("5m", MINUTES.toMillis(5))
    //    m15("15m", MINUTES.toMillis(15))
    //    m30("30m", MINUTES.toMillis(30))

    final Calendar calendar = marketEvent.calendar;

    // This must be consolidated to Million seconds
    calendar.setTimeInMillis(marketEvent.data.getOpenTime());

    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + 1;
    int min = calendar.get(calendar.MINUTE);
    int hour = calendar.get(calendar.HOUR_OF_DAY);
    int day = calendar.get(calendar.DAY_OF_MONTH);

    long minBase = year * 100000000L + month * 1000000L + day * 10000L + hour * 100L;

    final long m1Key = minBase + min,
        m3Key = minBase + (min / 3) * 3,
        m5Key = minBase + (min / 5) * 5,
        m15Key = minBase + (min / 15) * 15,
        m30Key = minBase + (min / 30) * 30;

    // 1. M1 force flush them
    // 2. Others update - recalculated them

    OHLCView m1 =
        repository.update(
            marketEvent.exchange, marketEvent.symbol, KlineInterval.m1, m1Key, marketEvent.data);

    if (marketEvent.isDerived()) {

      OHLCView m3 =
          repository.merger(
              marketEvent.exchange, marketEvent.symbol, KlineInterval.m3, m3Key, marketEvent.data);

      OHLCView m5 =
          repository.merger(
              marketEvent.exchange, marketEvent.symbol, KlineInterval.m5, m5Key, marketEvent.data);

      OHLCView m15 =
          repository.merger(
              marketEvent.exchange,
              marketEvent.symbol,
              KlineInterval.m15,
              m15Key,
              marketEvent.data);

      OHLCView m30 =
          repository.merger(
              marketEvent.exchange,
              marketEvent.symbol,
              KlineInterval.m30,
              m30Key,
              marketEvent.data);

      for (PostAggregateListener listener : listeners) {

        listener.batch(
            marketEvent.exchange,
            marketEvent.symbol,
            new KlineInterval[] {
              KlineInterval.m1,
              KlineInterval.m3,
              KlineInterval.m5,
              KlineInterval.m15,
              KlineInterval.m30,
            },
            new OHLCView[] {m1, m3, m5, m15, m30},
            true);
      }
    } else {

      KlineIntervalGroup group =
          ExchangeIntervalMapper.INSTANCE.group(marketEvent.exchange, KlineInterval.m1);

      if (group.getDerived().isEmpty()) {
        // Nothing to do
        for (PostAggregateListener listener : listeners) {
          listener.one(marketEvent.exchange, marketEvent.symbol, KlineInterval.m1, m1, false);
        }
      } else {
        // Still some need to do derived
        // KlineInterval.m3,
        // KlineInterval.m5,
        // KlineInterval.m15,
        // KlineInterval.m30

        List<OHLCView> datas = new ArrayList<>(1 + group.getDerived().size());
        List<KlineInterval> intervals = new ArrayList<>(1 + group.getDerived().size());

        datas.add(m1);
        intervals.add(KlineInterval.m1);

        for (final KlineInterval interval : group.getDerived()) {
          switch (interval) {
            case m3:
              OHLCView m31 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.m3,
                      m3Key,
                      marketEvent.data);
              datas.add(m31);
              intervals.add(KlineInterval.m3);
              break;
            case m5:
              OHLCView m51 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.m5,
                      m5Key,
                      marketEvent.data);
              datas.add(m51);
              intervals.add(KlineInterval.m5);
              break;
            case m15:
              OHLCView m151 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.m15,
                      m15Key,
                      marketEvent.data);
              datas.add(m151);
              intervals.add(KlineInterval.m15);
              break;
            case m30:
              OHLCView m301 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.m30,
                      m30Key,
                      marketEvent.data);
              datas.add(m301);
              intervals.add(KlineInterval.m30);
              break;
            default:
              logger.warn(
                  "MISS_DERIVED {},{},{},{}",
                  marketEvent.exchange,
                  KlineInterval.m1,
                  interval,
                  marketEvent.symbol);
              break;
          }
        }
        for (PostAggregateListener listener : listeners) {

          listener.batch(
              marketEvent.exchange,
              marketEvent.symbol,
              intervals.toArray(new KlineInterval[intervals.size()]),
              datas.toArray(new OHLCView[datas.size()]),
              false);
        }
      }
    }
  }

  private void tryAggregateOneHour(final MarketEvent marketEvent) {

    // Aggregate the
    // h2
    // h4
    // h6
    // h8
    // h12
    //    h1("1h", HOURS.toMillis(1))
    //    h2("2h", HOURS.toMillis(2))
    //    h4("4h", HOURS.toMillis(4))
    //    h6("6h", HOURS.toMillis(6))
    //    h8("8h", HOURS.toMillis(8))
    //    h12("12h", HOURS.toMillis(12))

    final Calendar calendar = marketEvent.calendar;

    // This must be consolidated to Million seconds
    calendar.setTimeInMillis(marketEvent.data.getOpenTime());

    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + 1;
    int hour = calendar.get(calendar.HOUR_OF_DAY);
    int day = calendar.get(calendar.DAY_OF_MONTH);

    long minBase = year * 1000000L + month * 10000L + day * 100L;

    final long h2Key = minBase + (hour / 2) * 2,
        h4Key = minBase + (hour / 4) * 4,
        h6Key = minBase + (hour / 6) * 6,
        h8Key = minBase + (hour / 8) * 8,
        h12Key = minBase + (hour / 12) * 12;

    OHLCView h1 =
        repository.update(
            marketEvent.exchange,
            marketEvent.symbol,
            KlineInterval.h1,
            minBase + hour,
            marketEvent.data);

    if (marketEvent.derived) {
      OHLCView h2 =
          repository.merger(
              marketEvent.exchange, marketEvent.symbol, KlineInterval.h2, h2Key, marketEvent.data);

      OHLCView h4 =
          repository.merger(
              marketEvent.exchange, marketEvent.symbol, KlineInterval.h4, h4Key, marketEvent.data);

      OHLCView h6 =
          repository.merger(
              marketEvent.exchange, marketEvent.symbol, KlineInterval.h6, h6Key, marketEvent.data);

      OHLCView h8 =
          repository.merger(
              marketEvent.exchange, marketEvent.symbol, KlineInterval.h8, h8Key, marketEvent.data);

      OHLCView h12 =
          repository.merger(
              marketEvent.exchange,
              marketEvent.symbol,
              KlineInterval.h12,
              h12Key,
              marketEvent.data);

      for (PostAggregateListener listener : listeners) {

        listener.batch(
            marketEvent.exchange,
            marketEvent.symbol,
            new KlineInterval[] {
              KlineInterval.h1,
              KlineInterval.h2,
              KlineInterval.h4,
              KlineInterval.h6,
              KlineInterval.h8,
              KlineInterval.h12,
            },
            new OHLCView[] {h1, h2, h4, h6, h8, h12},
            true);
      }
    } else {

      //    h2("2h", HOURS.toMillis(2))
      //    h4("4h", HOURS.toMillis(4))
      //    h6("6h", HOURS.toMillis(6))
      //    h8("8h", HOURS.toMillis(8))
      //    h12("12h", HOURS.toMillis(12))

      KlineIntervalGroup group =
          ExchangeIntervalMapper.INSTANCE.group(marketEvent.exchange, KlineInterval.h1);

      if (group.getDerived().isEmpty()) {
        // Nothing to do
        for (PostAggregateListener listener : listeners) {
          listener.one(marketEvent.exchange, marketEvent.symbol, KlineInterval.h1, h1, false);
        }
      } else {

        List<OHLCView> datas = new ArrayList<>(1 + group.getDerived().size());
        List<KlineInterval> intervals = new ArrayList<>(1 + group.getDerived().size());

        datas.add(h1);
        intervals.add(KlineInterval.h1);

        for (final KlineInterval interval : group.getDerived()) {
          switch (interval) {
            case h2:
              OHLCView h2 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.h2,
                      h2Key,
                      marketEvent.data);
              datas.add(h2);
              intervals.add(KlineInterval.h2);
              break;
            case h4:
              OHLCView h4 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.h4,
                      h4Key,
                      marketEvent.data);
              datas.add(h4);
              intervals.add(KlineInterval.h4);
              break;
            case h6:
              OHLCView h6 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.h6,
                      h6Key,
                      marketEvent.data);

              datas.add(h6);
              intervals.add(KlineInterval.h6);
              break;
            case h8:
              OHLCView h8 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.h8,
                      h8Key,
                      marketEvent.data);

              datas.add(h8);
              intervals.add(KlineInterval.h8);
              break;
            case h12:
              OHLCView h12 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.h12,
                      h12Key,
                      marketEvent.data);

              datas.add(h12);
              intervals.add(KlineInterval.h12);
              break;
            default:
              logger.warn(
                  "MISS_DERIVED {},{},{},{}",
                  marketEvent.exchange,
                  KlineInterval.h1,
                  interval,
                  marketEvent.symbol);
              break;
          }
        }
        //
        for (PostAggregateListener listener : listeners) {

          listener.batch(
              marketEvent.exchange,
              marketEvent.symbol,
              intervals.toArray(new KlineInterval[intervals.size()]),
              datas.toArray(new OHLCView[datas.size()]),
              false);
        }
      }
    }
  }

  private void tryAggregateOneDay(final MarketEvent marketEvent) {

    // Aggregate the
    // d3
    // w1
    // M1
    //    d1("1d", DAYS.toMillis(1))
    //    d3("3d", DAYS.toMillis(3))
    //    w1("1w", DAYS.toMillis(7))
    //    M1("1M", DAYS.toMillis(30))

    final Calendar calendar = marketEvent.calendar;

    // This must be consolidated to Million seconds
    calendar.setTimeInMillis(marketEvent.data.getOpenTime());

    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + 1;
    int day = calendar.get(calendar.DAY_OF_MONTH);

    long minBase = year * 10000L + month * 100L;

    long day3Key = minBase + (day / 3) * 3;

    // Month
    // year

    int dayOfWeek = calendar.get(calendar.DAY_OF_WEEK); // Sunday is 1

    long weekKey = minBase + day;

    if (dayOfWeek > Calendar.SUNDAY) {

      int sub = dayOfWeek - 1;
      calendar.add(Calendar.DAY_OF_MONTH, -sub);

      weekKey =
          calendar.get(Calendar.YEAR) * 10000
              + (calendar.get(Calendar.MONTH) + 1) * 100
              + calendar.get(calendar.DAY_OF_MONTH);
    }

    OHLCView d1 =
        repository.update(
            marketEvent.exchange,
            marketEvent.symbol,
            KlineInterval.d1,
            minBase + day,
            marketEvent.data);

    if (marketEvent.isDerived()) {
      OHLCView d3 =
          repository.merger(
              marketEvent.exchange,
              marketEvent.symbol,
              KlineInterval.d3,
              day3Key,
              marketEvent.data);

      OHLCView w1 =
          repository.merger(
              marketEvent.exchange,
              marketEvent.symbol,
              KlineInterval.w1,
              weekKey,
              marketEvent.data);

      OHLCView M1 =
          repository.merger(
              marketEvent.exchange,
              marketEvent.symbol,
              KlineInterval.M1,
              year * 100L + month,
              marketEvent.data);

      OHLCView Y1 =
          repository.merger(
              marketEvent.exchange, marketEvent.symbol, KlineInterval.Y1, year, marketEvent.data);

      for (PostAggregateListener listener : listeners) {

        listener.batch(
            marketEvent.exchange,
            marketEvent.symbol,
            new KlineInterval[] {
              KlineInterval.d1,
              KlineInterval.d3,
              KlineInterval.w1,
              KlineInterval.M1,
              KlineInterval.Y1
            },
            new OHLCView[] {d1, d3, w1, M1, Y1},
            true);
      }
    } else {
      KlineIntervalGroup group =
          ExchangeIntervalMapper.INSTANCE.group(marketEvent.exchange, KlineInterval.d1);

      if (group.getDerived().isEmpty()) {
        // Nothing to do
        for (PostAggregateListener listener : listeners) {
          listener.one(marketEvent.exchange, marketEvent.symbol, KlineInterval.d1, d1, false);
        }
      } else {

        List<OHLCView> datas = new ArrayList<>(1 + group.getDerived().size());
        List<KlineInterval> intervals = new ArrayList<>(1 + group.getDerived().size());

        datas.add(d1);
        intervals.add(KlineInterval.d1);

        for (final KlineInterval interval : group.getDerived()) {
          switch (interval) {
            case d3:
              OHLCView d3 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.d3,
                      day3Key,
                      marketEvent.data);
              datas.add(d3);
              intervals.add(KlineInterval.d3);
              break;
            case w1:
              OHLCView w1 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.w1,
                      weekKey,
                      marketEvent.data);
              datas.add(w1);
              intervals.add(KlineInterval.w1);
              break;
            case M1:
              OHLCView M1 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.M1,
                      year * 100L + month,
                      marketEvent.data);

              datas.add(M1);
              intervals.add(KlineInterval.M1);
              break;
            case Y1:
              OHLCView Y1 =
                  repository.merger(
                      marketEvent.exchange,
                      marketEvent.symbol,
                      KlineInterval.Y1,
                      year,
                      marketEvent.data);

              datas.add(Y1);
              intervals.add(KlineInterval.Y1);
              break;
            default:
              logger.warn(
                  "MISS_DERIVED {},{},{},{}",
                  marketEvent.exchange,
                  KlineInterval.d1,
                  interval,
                  marketEvent.symbol);
              break;
          }
        }

        for (PostAggregateListener listener : listeners) {

          listener.batch(
              marketEvent.exchange,
              marketEvent.symbol,
              intervals.toArray(new KlineInterval[intervals.size()]),
              datas.toArray(new OHLCView[datas.size()]),
              false);
        }
      }
    }
  }

  private void triggerCheckPoint(final boolean dead) {}

  @Override
  public void onStart() {
    logger.warn("START_AGG_HANDLER {}", segmentId);
  }

  @Override
  public void onShutdown() {
    logger.warn("SHUT_DOWN_AGG_HANDLER {}", segmentId);
  }
}
