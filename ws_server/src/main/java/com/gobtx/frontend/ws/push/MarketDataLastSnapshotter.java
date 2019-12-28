package com.gobtx.frontend.ws.push;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;

import java.util.List;

/** Created by Aaron Kuai on 2019/12/6. */
public interface MarketDataLastSnapshotter {

  OHLCWithExchangeAndIntervalView handle(final OHLCWithExchangeAndIntervalView data);

  List<OHLCWithExchangeAndIntervalView> lastSnapshot();

  List<OHLCWithExchangeAndIntervalView> lastSnapshot(final KlineInterval klineInterval);

  List<OHLCWithExchangeAndIntervalView> lastSnapshot(
      final Exchange exchange, final KlineInterval interval);

  OHLCWithExchangeAndIntervalView lastSnapshot(
      final Exchange exchange, final KlineInterval klineInterval, final String symbol);
}
