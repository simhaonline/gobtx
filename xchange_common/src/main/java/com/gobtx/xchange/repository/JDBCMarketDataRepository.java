package com.gobtx.xchange.repository;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;

import java.util.Optional;

/** Created by Aaron Kuai on 2019/11/11. */
public class JDBCMarketDataRepository implements MarketDataRepository {

  @Override
  public Optional<OHLCView> getByExchangeAndTypeAndKey(
      Exchange exchange, String symbol, KlineInterval type, long key) {
    return Optional.empty();
  }

  @Override
  public OHLCView merger(
      Exchange exchange, String symbol, KlineInterval type, long key, OHLCView data) {

    throw new IllegalStateException("not implement yet");
  }

  @Override
  public OHLCView update(
      Exchange exchange, String symbol, KlineInterval type, long key, OHLCView data) {
    throw new IllegalStateException("not implement yet");
  }
}
