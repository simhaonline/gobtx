package com.gobtx.xchange.repository;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;

import java.util.Optional;

/** Created by Aaron Kuai on 2019/11/11. */
public interface MarketDataRepository {

  /**
   * According to the exchange pick up the specific OHLC this is read only return value
   *
   * @param exchange
   * @param symbol
   * @param type
   * @param key
   * @return
   * @deprecated not used yet
   */
  Optional<OHLCView> getByExchangeAndTypeAndKey(
      final Exchange exchange, final String symbol, final KlineInterval type, final long key);

  /**
   * Merger is a compute logic so not fully overwrite the original data
   *
   * @param exchange
   * @param symbol
   * @param type
   * @param key
   * @param data
   */
  OHLCView merger(
      final Exchange exchange,
      final String symbol,
      final KlineInterval type,
      final long key,
      final OHLCView data);

  /**
   * Update is to tally over write the original data
   *
   * @param exchange
   * @param symbol
   * @param type
   * @param key
   * @param data
   */
  OHLCView update(
      final Exchange exchange,
      final String symbol,
      final KlineInterval type,
      final long key,
      final OHLCView data);

  /** Trigger the batch flush to disk if any needed */
  default void flush() {
    // Default nothing to do
  }
}
