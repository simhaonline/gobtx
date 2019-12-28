package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

/** Created by Aaron Kuai on 2019/12/12. */
public class AlwaysFromVeryBeginMarketBatchCutoffTimeFinder implements MarketBatchCutoffTimeFinder {

  public static final AlwaysFromVeryBeginMarketBatchCutoffTimeFinder INSTANCE =
      new AlwaysFromVeryBeginMarketBatchCutoffTimeFinder();

  private AlwaysFromVeryBeginMarketBatchCutoffTimeFinder() {}

  @Override
  public long cutoffTime(Exchange exchange, KlineInterval interval, String symbol) {
    return System.currentTimeMillis() - interval.getHistoryLength();
  }
}
