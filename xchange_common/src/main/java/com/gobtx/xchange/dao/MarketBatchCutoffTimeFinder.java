package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

/** Created by Aaron Kuai on 2019/12/12. */
public interface MarketBatchCutoffTimeFinder {

  default long cutoffTime(
      final Exchange exchange, final KlineInterval interval, final String symbol) {
    return -1;
  }
}
