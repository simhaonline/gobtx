package com.gobtx.xchange;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;

/** Created by Aaron Kuai on 2019/11/11. */
public interface MarketDataStreamListener {

  void update(
      final OHLCView data,
      final KlineInterval interval,
      final Exchange exchange,
      final String symbol,
      final boolean derived);

  default void update(
      final OHLCView data,
      final KlineInterval interval,
      final Exchange exchange,
      final String symbol) {
    update(data, interval, exchange, symbol, true);
  }
}
