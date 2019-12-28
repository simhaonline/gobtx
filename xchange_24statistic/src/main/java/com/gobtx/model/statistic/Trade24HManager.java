package com.gobtx.model.statistic;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Created by Aaron Kuai on 2019/12/23. */
public class Trade24HManager implements Trade24HProviderListener {

  static final Logger logger = LoggerFactory.getLogger(Trade24HManager.class);

  // Will this be the thing?

  public static final Trade24HManager INSTANCE = new Trade24HManager();

  protected final ConcurrentHashMap<String, Trade24HStatistic>[] lastStatisticMap =
      new ConcurrentHashMap[Exchange.VALS.length];

  private Trade24HManager() {

    for (int i = 0; i < Exchange.VALS.length; i++) {
      lastStatisticMap[i] = new ConcurrentHashMap<>();
    }
  }

  protected final AtomicBoolean started = new AtomicBoolean(false);

  public List<Trade24HStatistic> statistics() {

    List<Trade24HStatistic> res = new ArrayList<>();

    for (final ConcurrentHashMap<String, Trade24HStatistic> row : lastStatisticMap) {

      if (!row.isEmpty()) {
        res.addAll(row.values());
      }
    }
    return res;
  }

  @Override
  public void update(Trade24HStatistic statistic) {
    final ConcurrentHashMap<String, Trade24HStatistic> target =
        lastStatisticMap[statistic.getExchange().ordinal()];

    target.compute(
        statistic.getSymbol(),
        (k, v) -> {
          if (v == null) {
            return statistic;
          } else {

            if (v.getTimestamp() > statistic.getTimestamp()) {
              return v;
            } else {
              return statistic;
            }
          }
        });
  }

  public void handle(final OHLCWithExchangeAndIntervalView data) {
    if (KlineInterval.m1 == data.getInterval()) {

      final ConcurrentHashMap<String, Trade24HStatistic> target =
          lastStatisticMap[data.getExchange().ordinal()];

      target.computeIfPresent(
          data.getSymbol(),
          (k, v) -> {
            if (v.getTimestamp() <= data.getCloseTime()) {

              v.setTimestamp(data.getCloseTime()).setClose(data.getClose());
              if (v.getHigh().compareTo(data.getClose()) < 0) {
                v.setHigh(data.getHigh());
              }
            }
            return v;
          });
    }
  }

  @SuppressWarnings("Duplicates")
  public void start(final List<Trade24HProvider> trade24HProviders) {

    if (started.compareAndSet(false, true)) {

      for (Trade24HProvider provider : trade24HProviders) {

        logger.debug("TRADE_24HR_PROVIDER {},{}", provider.exchange(), provider.getClass());

        final ConcurrentHashMap<String, Trade24HStatistic> target =
            lastStatisticMap[provider.exchange().ordinal()];

        provider.listener(this);

        provider.start();

        //        provider
        //            .statistic()
        //            .forEach(
        //                it -> {
        //                  target.compute(
        //                      it.getSymbol(),
        //                      (k, v) -> {
        //                        if (v == null) {
        //                          return it;
        //                        } else {
        //
        //                          if (v.getTimestamp() > it.getTimestamp()) {
        //                            return v;
        //                          } else {
        //                            return it;
        //                          }
        //                        }
        //                      });
        //                });
      }
    }
  }
}
