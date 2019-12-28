package com.gobtx.xchange.dao;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/** Created by Aaron Kuai on 2019/11/14. */
public class MarketDataLoaderWriter
    implements MapLoader<OHLCKeyData, OHLCData>,
        MapStore<OHLCKeyData, OHLCData>,
        MapLoaderLifecycleSupport {

  static final Logger logger = LoggerFactory.getLogger(MarketDataLoaderWriter.class);

  protected String name;

  protected Flusher flusher;

  @Override
  public void init(
      final HazelcastInstance hazelcastInstance,
      final Properties properties,
      final String mapName) {

    // INIT_CACHE huobi_1m,{exchange=HUOBI, interval=m1}
    // This may used to inject the value from the external
    // Like the spring context
    name = mapName;
    logger.debug("INIT_CACHE {},{}", mapName, properties);

    final String exchange = properties.getProperty("exchange");
    final String interval = properties.getProperty("interval");

    if (exchange == null || interval == null) {
      throw new IllegalStateException("MKT_CACHE_MUST_CONTAIN exchange & interval");
    }

    final Exchange ex = Exchange.valueOf(exchange);
    final KlineInterval kline = KlineInterval.valueOf(interval);

    flusher = MarketDataFlusherContext.flusher(ex, kline);

    logger.warn("MKT_CACHE_DS_CONNECT {},{},{}", exchange, interval, flusher);
  }

  @Override
  public void destroy() {
    logger.warn("CACHE_DESTROY {}", name);
  }

  @Override
  public void store(OHLCKeyData ohlcKeyData, OHLCData data) {
    flusher.save(data);
  }

  @Override
  public void storeAll(Map<OHLCKeyData, OHLCData> map) {
    flusher.batch(map.values());
  }

  @Override
  public void delete(OHLCKeyData ohlcKeyData) {
    // Nothing to do
  }

  @Override
  public void deleteAll(Collection<OHLCKeyData> collection) {
    // Nothing to do
  }

  @Override
  public OHLCData load(OHLCKeyData ohlcKeyData) {
    return flusher.get(ohlcKeyData);
  }

  @Override
  public Map<OHLCKeyData, OHLCData> loadAll(final Collection<OHLCKeyData> keys) {
    logger.warn("TRY_LOAD_ALL_CACHE {}", keys);
    return Collections.emptyMap();
  }

  @Override
  public Iterable<OHLCKeyData> loadAllKeys() {
    return Collections.emptyList();
  }
}
