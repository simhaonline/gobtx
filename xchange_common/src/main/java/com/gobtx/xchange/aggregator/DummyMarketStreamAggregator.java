package com.gobtx.xchange.aggregator;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Aaron Kuai on 2019/11/13. */
public class DummyMarketStreamAggregator implements MarketStreamAggregator {

  static final Logger logger = LoggerFactory.getLogger(DummyMarketStreamAggregator.class);

  @Override
  public void update(
      OHLCView data, KlineInterval interval, Exchange exchange, String symbol, boolean derived) {
    logger.warn("DUM_MKT_STREAM_AGGREGATOR {},{},{},{}", exchange, symbol, interval, data);
  }

  @Override
  public void start() {
    logger.warn("DUM_MKT_STREAM_START");
  }

  @Override
  public void stop() {
    logger.warn("DUM_MKT_STREAM_STOP");
  }

  @Override
  public Closeable postAggregateListener(PostAggregateListener listener) {
    logger.warn("DUM_MKT_POST_AGG_LISTENER_REGISTER {}", listener);
    return () -> {};
  }
}
