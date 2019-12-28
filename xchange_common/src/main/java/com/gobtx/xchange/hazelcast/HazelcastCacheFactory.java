package com.gobtx.xchange.hazelcast;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.hazelcast.core.IMap;

/** Created by Aaron Kuai on 2019/11/12. */
@FunctionalInterface
public interface HazelcastCacheFactory {

  IMap<OHLCKeyData, OHLCData> cache(
      final Exchange exchange, final String symbol, final KlineInterval interval);
}
