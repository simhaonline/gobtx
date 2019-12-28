package com.gobtx.xchange.repository;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Created by Aaron Kuai on 2019/11/11. */
public class LocalTestMarketDataRepository implements MarketDataRepository {

  static final Logger logger = LoggerFactory.getLogger(LocalTestMarketDataRepository.class);

  /** Exchange **** <---->K Like Type ********<---->Symbol ************Key <----> Data */
  public final Map<Exchange, Map<KlineInterval, Map<String, TreeMap<Long, OHLCData>>>> cache =
      new ConcurrentHashMap<>();

  final Function<Exchange, Map<KlineInterval, Map<String, TreeMap<Long, OHLCData>>>> L1_FACTORY =
      exchange -> new ConcurrentHashMap<>();

  final Function<KlineInterval, Map<String, TreeMap<Long, OHLCData>>> L2_FACTORY =
      interval -> new ConcurrentHashMap();

  final Function<String, TreeMap<Long, com.gobtx.model.domain.OHLCData>> L3_FACTORY =
      s -> new TreeMap();

  @Override
  public void flush() {}

  @Override
  public OHLCView merger(
      final Exchange exchange,
      final String symbol,
      final KlineInterval type,
      final long key,
      final OHLCView data) {

    return cache
        .computeIfAbsent(exchange, L1_FACTORY)
        .computeIfAbsent(type, L2_FACTORY)
        .computeIfAbsent(symbol, L3_FACTORY)
        .compute(
            key,
            (key1, value) -> {
              if (value != null) {
                // Possible out of order?

                if (data.getCloseTime() <= data.getCloseTime()) {

                  value.setCloseTime(value.getCloseTime());

                  if (data.getLow().compareTo(value.getLow()) < 0) {
                    value.setLow(data.getLow());
                  }

                  if (data.getHigh().compareTo(value.getHigh()) > 0) {
                    value.setHigh(data.getHigh());
                  }

                  value
                      .setVolume(value.getVolume().add(data.getVolume()))
                      .setAmount(value.getAmount().add(data.getAmount()))
                      .setNumberOfTrades(value.getNumberOfTrades() + (data.getNumberOfTrades()));

                } else {
                  // This is Chaos of the market stream
                  logger.warn("CHAOS_OF_TIME {},{},{}", symbol, type, key);
                }

              } else {

                return OHLCDataImpl.copy(data);
              }
              return value;
            });
  }

  @Override
  public OHLCView update(
      Exchange exchange, final String symbol, KlineInterval type, long key, OHLCView data) {

    OHLCData res = (OHLCDataImpl.copy(data));
    cache
        .computeIfAbsent(exchange, L1_FACTORY)
        .computeIfAbsent(type, L2_FACTORY)
        .computeIfAbsent(symbol, L3_FACTORY)
        .put(key, res);
    return res;
  }

  @Override
  public Optional<OHLCView> getByExchangeAndTypeAndKey(
      Exchange exchange, String symbol, KlineInterval type, long key) {

    final Map<KlineInterval, Map<String, TreeMap<Long, OHLCData>>> exData = cache.get(exchange);

    if (exData == null || exData.isEmpty()) {
      return Optional.empty();
    } else {
      Map<String, TreeMap<Long, OHLCData>> table = exData.get(type);

      if (table == null || table.isEmpty()) {
        return Optional.empty();
      } else {

        TreeMap<Long, OHLCData> row = table.get(symbol);

        if (row != null || row.isEmpty()) {
          return Optional.of(row.get(key));
        } else {
          return Optional.empty();
        }
      }
    }
  }
}
