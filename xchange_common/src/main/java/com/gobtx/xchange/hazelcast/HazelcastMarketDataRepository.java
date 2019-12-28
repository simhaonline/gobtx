package com.gobtx.xchange.hazelcast;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import com.gobtx.xchange.repository.MarketDataRepository;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.gobtx.model.domain.OHLCKeyDataImpl.build;

/**
 * Created by Aaron Kuai on 2019/11/12.
 *
 * <p>Different strategy may need will them need individual cache storage or Share the same one or
 * each exchange suppose has one
 */
public class HazelcastMarketDataRepository implements MarketDataRepository {

  static final Logger logger = LoggerFactory.getLogger(HazelcastMarketDataRepository.class);

  static boolean debugEnable = logger.isDebugEnabled();

  protected final HazelcastCacheFactory factory;

  public HazelcastMarketDataRepository(HazelcastCacheFactory factory) {
    this.factory = factory;
  }

  @Override
  public Optional<OHLCView> getByExchangeAndTypeAndKey(
      final Exchange exchange, final String symbol, final KlineInterval type, final long key) {

    final IMap<OHLCKeyData, OHLCData> cache = factory.cache(exchange, symbol, type);

    OHLCData data = cache.get(build(symbol, key));

    return Optional.of(data);
  }

  @Override
  public OHLCView merger(
      Exchange exchange, String symbol, KlineInterval type, long key, OHLCView data) {

    final IMap<OHLCKeyData, OHLCData> cache = factory.cache(exchange, symbol, type);

    if (debugEnable) {
      logger.debug("REPOSITORY_MERGER {},{},{},{}", exchange, symbol, type.code(), key);
    }

    return (OHLCView)
        cache.executeOnKey(
            build(symbol, key),
            new HazelcastEntryProcessor(
                data.getOpenTime(),
                data.getCloseTime(),
                data.getOpen(),
                data.getHigh(),
                data.getLow(),
                data.getClose(),
                data.getVolume(),
                data.getAmount(),
                data.getNumberOfTrades()));
  }

  @Override
  public OHLCView update(
      Exchange exchange, String symbol, KlineInterval type, long key, OHLCView data) {

    if (debugEnable) {
      logger.debug("REPOSITORY_UPDATE {},{},{},{}", exchange, symbol, type.code(), key);
    }

    final OHLCData ohlc = OHLCDataImpl.copy(data).setTimeKey(key);
    factory.cache(exchange, symbol, type).put(build(symbol, key), ohlc);
    return ohlc;
  }
}
