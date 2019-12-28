package com.gobtx.frontend.ws.hazelcast;

import com.gobtx.frontend.ws.service.MarketDataService;
import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.hazelcast.query.Predicates.*;

/** Created by Aaron Kuai on 2019/12/4. */
public class HazelcastMarketDataService implements MarketDataService {

  @Autowired protected HazelcastInstance instance;

  protected Map<String, IMap<OHLCKeyData, OHLCData>> localValueHolder = new ConcurrentHashMap<>();

  protected final Function<String, IMap<OHLCKeyData, OHLCData>> factory =
      new Function<String, IMap<OHLCKeyData, OHLCData>>() {
        @Override
        public IMap<OHLCKeyData, OHLCData> apply(String s) {
          return instance.getMap(s);
        }
      };

  private IMap<OHLCKeyData, OHLCData> cache(final Exchange exchange, final KlineInterval interval) {
    return localValueHolder.computeIfAbsent(exchange.cacheName(interval), factory);
  }

  final Comparator<OHLCData> sort = (o1, o2) -> (o1.getOpenTime() - o2.getOpenTime()) > 0 ? 1 : -1;

  @Override
  public List<OHLCData> data(
      final String symbol,
      final KlineInterval interval,
      final Exchange exchange,
      final long startTime,
      final long endTime,
      boolean first) {

    final IMap<OHLCKeyData, OHLCData> cache = cache(exchange, interval);

    if (cache != null) {

      final Predicate namePredicate = equal("symbol", symbol);
      final Predicate timeStartPredicate = greaterEqual("openTime", startTime);
      final Predicate timeEndPredicate = lessEqual("openTime", endTime);

      final Predicate predicate = and(namePredicate, timeStartPredicate, timeEndPredicate);

      final Collection<OHLCData> res = cache.values(predicate);

      if (res.isEmpty() || res.size() == 1) {
        return new ArrayList<>(res);
      } else {
        // Check the order? I not sure it is desc or asc
        // Better test local
        final List<OHLCData> got = new ArrayList<>(res);
        Collections.sort(got, sort);
        return got;
      }
    }

    return Collections.EMPTY_LIST;
  }

  @Override
  public int order() {
    return 1;
  }
}
