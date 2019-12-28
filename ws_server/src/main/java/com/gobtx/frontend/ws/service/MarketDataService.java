package com.gobtx.frontend.ws.service;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;

import java.util.List;

/** Created by Aaron Kuai on 2019/11/8. */
public interface MarketDataService {

  default void start() {}

  default void stop() {}

  default int order() {
    return 0;
  }

  List<? extends OHLCView> data(
      final String symbol,
      final KlineInterval interval,
      final Exchange exchange,
      final long startTime,
      final long endTime,
      final boolean first);

  // How to do the asyn jobs?
  // 1. call back
  // 2. invoke handler

}
