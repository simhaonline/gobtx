package com.gobtx.xchange.service.statistic;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/** Created by Aaron Kuai on 2019/11/13. */
public class DummyMarketDataServiceStatistic implements MarketDataServiceStatistic {

  static final Logger logger = LoggerFactory.getLogger(DummyMarketDataServiceStatistic.class);

  private final Exchange exchange;

  private DummyMarketDataServiceStatistic(Exchange exchange) {

    // TODO: 2019/11/13  remember move to the metrics influx DB etc

    this.exchange = exchange;
  }

  public static final MarketDataServiceStatistic dummyStatistic(final Exchange exchange) {
    return new DummyMarketDataServiceStatistic(exchange);
  }

  @Override
  public void count(final String symbol, final KlineInterval interval, final int count) {

    if (logger.isDebugEnabled()) {
      logger.debug("STATISTIC {},{},{},{}", exchange, symbol, interval, count);
    }
  }

  @Override
  public Map<String, Map<KlineInterval, Long>> snapshot() {
    return Collections.emptyMap();
  }
}
