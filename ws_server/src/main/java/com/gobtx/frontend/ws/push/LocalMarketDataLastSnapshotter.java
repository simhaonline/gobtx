package com.gobtx.frontend.ws.push;

import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** Created by Aaron Kuai on 2019/12/6. */
public class LocalMarketDataLastSnapshotter implements MarketDataLastSnapshotter {

  // List[][] {k-v}
  protected final ConcurrentHashMap<String, OHLCWithExchangeAndIntervalView>[][] lastSnapshotCache =
      new ConcurrentHashMap[Exchange.VALS.length][];

  public static final LocalMarketDataLastSnapshotter INSTANCE =
      new LocalMarketDataLastSnapshotter();

  protected LocalMarketDataLastSnapshotter() {

    for (int i = 0; i < Exchange.VALS.length; i++) {

      lastSnapshotCache[i] = new ConcurrentHashMap[KlineInterval.VALS.length];

      for (int j = 0; j < KlineInterval.VALS.length; j++) {
        lastSnapshotCache[i][j] = new ConcurrentHashMap<>();
      }
    }
  }

  @Override
  public OHLCWithExchangeAndIntervalView handle(OHLCWithExchangeAndIntervalView data) {

    return lastSnapshotCache[data.getExchange().ordinal()][data.getInterval().ordinal()].compute(
        data.getSymbol(),
        (k, v) -> {
          if (v == null) {
            return data;
          } else {
            if (v.getOpenTime() < data.getOpenTime()) {
              return data;
            } else {
              return v;
            }
          }
        });
  }

  @Override
  public List<OHLCWithExchangeAndIntervalView> lastSnapshot() {
    // Return a List:
    final List<OHLCWithExchangeAndIntervalView> snapshot = new ArrayList<>(32);

    for (int i = 0; i < Exchange.VALS.length; i++) {
      for (int j = 0; j < KlineInterval.VALS.length; j++) {
        if (!lastSnapshotCache[i][j].isEmpty()) {
          snapshot.addAll(lastSnapshotCache[i][j].values());
        }
      }
    }
    return snapshot;
  }

  @Override
  public OHLCWithExchangeAndIntervalView lastSnapshot(
      Exchange exchange, KlineInterval klineInterval, String symbol) {

    return lastSnapshotCache[exchange.ordinal()][klineInterval.ordinal()].getOrDefault(
        symbol, OHLCWithExchangeAndInternalData.DEAD);
  }

  @Override
  public List<OHLCWithExchangeAndIntervalView> lastSnapshot(KlineInterval klineInterval) {
    final List<OHLCWithExchangeAndIntervalView> snapshot = new ArrayList<>(32);
    for (int i = 0; i < Exchange.VALS.length; i++) {
      if (!lastSnapshotCache[i][klineInterval.ordinal()].isEmpty()) {
        snapshot.addAll(lastSnapshotCache[i][klineInterval.ordinal()].values());
      }
    }
    return snapshot;
  }

  @Override
  public List<OHLCWithExchangeAndIntervalView> lastSnapshot(
      Exchange exchange, KlineInterval interval) {
    if (!lastSnapshotCache[exchange.ordinal()][interval.ordinal()].isEmpty()) {
      return new ArrayList<>(lastSnapshotCache[exchange.ordinal()][interval.ordinal()].values());
    }

    return Collections.emptyList();
  }
}
