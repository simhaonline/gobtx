package com.gobtx.xchange.service.statistic;

import com.gobtx.model.enums.KlineInterval;

import java.util.Map;

/** Created by Aaron Kuai on 2019/11/13. */
public interface MarketDataServiceStatistic {

  default void count(final String symbol, final KlineInterval interval) {
    count(symbol, interval, 1);
  }

  void count(final String symbol, final KlineInterval interval, final int count);

  /**
   * Last refresh snapshot of the Kline type
   *
   * @return
   */
  Map<String, Map<KlineInterval, Long>> snapshot();
}
