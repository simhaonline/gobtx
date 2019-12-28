package com.gobtx.xchange.service;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.MarketStreamFeed;
import com.gobtx.xchange.exception.NotYetImplementedForExchangeException;
import com.gobtx.xchange.service.statistic.MarketDataServiceStatistic;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

/** Created by Aaron Kuai on 2019/11/13. */
public interface MarketDataService extends MarketStreamFeed, BaseService {

  default Stream<OHLCData> history(
      final KlineInterval interval, final String symbol, final int limit, final long cutTimestamp) {

    throw new NotYetImplementedForExchangeException(
        "History market API not ready for " + exchange());
  }

  default void start() {
    // DO nothing
  }

  default void stop() {
    // Do nothing
  }

  default Set<KlineInterval> supportKlineIntervals() {
    return Collections.emptySet();
  }

  default Set<KlineInterval> supportReconKlineIntervals() {
    return supportKlineIntervals();
  }

  /**
   * Start trigger the real time stream line of the kline feeds
   *
   * @param intervals
   * @param symbols
   * @return
   */
  Closeable subscribe(final KlineInterval[] intervals, final String... symbols);

  /**
   * Get the statistic of the market data feed
   *
   * @return
   */
  MarketDataServiceStatistic statistic();

  /**
   * This is to subscribe the trade executed event
   *
   * @param symbols
   * @return
   */
  default Closeable subscribeTradeEvent(final String... symbols) {

    return () -> {};
  }
}
