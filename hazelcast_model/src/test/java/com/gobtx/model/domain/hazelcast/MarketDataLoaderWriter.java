package com.gobtx.model.domain.hazelcast;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCKeyData;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/** Created by Aaron Kuai on 2019/11/14. */
public class MarketDataLoaderWriter
    implements MapLoader<OHLCKeyData, OHLCData>,
        MapStore<OHLCKeyData, OHLCData>,
        MapLoaderLifecycleSupport {

  protected String name;

  @Override
  public void init(
      final HazelcastInstance hazelcastInstance,
      final Properties properties,
      final String mapName) {

    // This may used to inject the value from the external
    // Like the spring context
    name = mapName;
    System.err.println("INIT_CACHE {},{}" + mapName + properties);
  }

  @Override
  public void destroy() {}

  @Override
  public void store(OHLCKeyData ohlcKeyData, OHLCData data) {
    System.err.println("TRY_SAVE_CACHE {},{}" + name + ohlcKeyData);
  }

  @Override
  public void storeAll(Map<OHLCKeyData, OHLCData> map) {
    System.err.println("TRY_SAVE_ALL_CACHE {},{}" + name + map.keySet());
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
    System.err.println("TRY_LOAD_DATA {}" + ohlcKeyData);
    return null;
  }

  @Override
  public Map<OHLCKeyData, OHLCData> loadAll(final Collection<OHLCKeyData> keys) {
    System.err.println("TRY_LOAD_ALL_CACHE {}" + keys);
    return Collections.emptyMap();
  }

  @Override
  public Iterable<OHLCKeyData> loadAllKeys() {
    return Collections.emptyList();
  }
}
