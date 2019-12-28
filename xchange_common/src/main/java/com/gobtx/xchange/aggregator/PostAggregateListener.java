package com.gobtx.xchange.aggregator;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;

/** Created by Aaron Kuai on 2019/11/11. */
public interface PostAggregateListener {

  void one(
      final Exchange exchange,
      final String symbol,
      final KlineInterval interval,
      final OHLCView data,
      final boolean derived);

  void batch(
      final Exchange exchange,
      final String symbol,
      final KlineInterval[] intervals,
      final OHLCView[] dataList,
      final boolean derived);
}
