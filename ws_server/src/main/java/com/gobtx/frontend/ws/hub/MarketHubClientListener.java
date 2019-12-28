package com.gobtx.frontend.ws.hub;

import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import com.gobtx.model.view.TradeEventWithExchangeView;

/** Created by Aaron Kuai on 2019/11/20. */
public interface MarketHubClientListener {

  void handle(final OHLCWithExchangeAndIntervalView data);

  default void start() {}

  default void stop() {}

  void handleTradeEvent(TradeEventWithExchangeView msg);
}
