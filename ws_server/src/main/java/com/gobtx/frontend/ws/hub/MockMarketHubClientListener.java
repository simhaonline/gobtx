package com.gobtx.frontend.ws.hub;

import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import com.gobtx.model.view.TradeEventWithExchangeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Created by Aaron Kuai on 2019/11/21. */
@Component
@Profile("mock-pusher")
public class MockMarketHubClientListener implements MarketHubClientListener {

  static final Logger logger = LoggerFactory.getLogger(MockMarketHubClientListener.class);

  @Override
  public void handle(OHLCWithExchangeAndIntervalView data) {

    logger.debug(
        "MOCK_PUSH_MARKET {},{},{}", data.getExchange(), data.getInterval(), data.getSymbol());
  }

  @Override
  public void start() {
    logger.debug("START_MOCK_PUSHER");
  }

  @Override
  public void stop() {
    logger.debug("STOP_MOCK_PUSHER");
  }

  @Override
  public void handleTradeEvent(TradeEventWithExchangeView msg) {
    logger.debug("MOCK_PUSH_TRADE {},{}", msg.getExchange(), msg.getSymbol());
  }
}
