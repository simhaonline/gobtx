package com.gobtx.xchange;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.domain.OHLCKeyDataImpl;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static com.hazelcast.query.Predicates.*;
import static java.util.concurrent.TimeUnit.MINUTES;

/** Created by Aaron Kuai on 2019/12/6. */
public class TestHazelcastOrder {

  @Test
  public void testOrder() {
    final InputStream configInputStream =
        TestHazelcastOrder.class.getClassLoader().getResourceAsStream("Hazelcast.xml");

    Config config = new XmlConfigBuilder(configInputStream).build();
    config.setLicenseKey("FOR_EVER_YOUNG");

    HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);

    IMap<OHLCKeyData, OHLCData> cache = hazelcast.getMap("template");

    List<OHLCDataImpl> mock =
        Arrays.asList(
            new OHLCDataImpl()
                .setSymbol("USDJPY")
                .setTimeKey(1)
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(6))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(5))
                .setOpen(BigDecimal.valueOf(66666))
                .setHigh(BigDecimal.valueOf(66666))
                .setLow(BigDecimal.valueOf(66666))
                .setVolume(BigDecimal.valueOf(6666))
                .setClose(BigDecimal.valueOf(6666)),
            new OHLCDataImpl()
                .setSymbol("USDJPY")
                .setTimeKey(2)
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(5))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(4))
                .setOpen(BigDecimal.valueOf(55555))
                .setHigh(BigDecimal.valueOf(5555))
                .setLow(BigDecimal.valueOf(55555))
                .setVolume(BigDecimal.valueOf(5555))
                .setClose(BigDecimal.valueOf(5555)),
            new OHLCDataImpl()
                .setSymbol("USDJPY")
                .setTimeKey(3)
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(4))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(3))
                .setOpen(BigDecimal.valueOf(44444))
                .setHigh(BigDecimal.valueOf(44444))
                .setLow(BigDecimal.valueOf(4444))
                .setVolume(BigDecimal.valueOf(4444))
                .setClose(BigDecimal.valueOf(4444)),
            new OHLCDataImpl()
                .setSymbol("USDJPY")
                .setTimeKey(4)
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(3))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(2))
                .setOpen(BigDecimal.valueOf(3333))
                .setHigh(BigDecimal.valueOf(3333))
                .setLow(BigDecimal.valueOf(3333))
                .setVolume(BigDecimal.valueOf(3333))
                .setClose(BigDecimal.valueOf(3333)));

    for (OHLCDataImpl each : mock) {
      cache.put(new OHLCKeyDataImpl(each.getSymbol(), each.getOpenTime()), each);
    }

    Predicate namePredicate = equal("symbol", "USDJPY");
    Predicate timeStartPredicate =
        greaterEqual("openTime", System.currentTimeMillis() - MINUTES.toMillis(8));

    Predicate predicate = and(namePredicate, timeStartPredicate);

    final Collection<OHLCData> res = cache.values(predicate);

    final List<OHLCData> sorted = new ArrayList<>(res);
    Collections.sort(sorted, (o1, o2) -> (o1.getOpenTime() - o2.getOpenTime()) > 0 ? 1 : -1);

    System.out.println(res.getClass());

    for (final OHLCData dd : sorted) {
      // It is not fuck ordered

      // 1575611997540  55555
      // 1575612117540  3333
      // 1575612057540  44444
      // 1575611937539  66666

      System.out.println(dd.getOpenTime() + "  " + dd.getOpen() + " " + dd.getTimeKey());
    }
  }
}
