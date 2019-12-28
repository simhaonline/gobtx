package com.gobtx.frontend.ws.jdbc;

import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static java.util.concurrent.TimeUnit.MINUTES;

/** Created by Aaron Kuai on 2019/12/10. */
public class JDBCMarketDataServiceTest {

  @Test
  public void testThings() {

    // TODO: 2019/12/10 a

    JDBCMarketDataService service = new JDBCMarketDataService(Collections.emptyList());

    final TreeSet<OHLCWithExchangeAndIntervalView> data = new TreeSet<>(service.sorter);

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
                .setOpen(BigDecimal.valueOf(33333))
                .setHigh(BigDecimal.valueOf(33333))
                .setLow(BigDecimal.valueOf(33333))
                .setVolume(BigDecimal.valueOf(33333))
                .setClose(BigDecimal.valueOf(33333)));

    final StubOHLCWithExchangeAndIntervalView start =
        new StubOHLCWithExchangeAndIntervalView(mock.get(1).getOpenTime());

    final StubOHLCWithExchangeAndIntervalView end =
        new StubOHLCWithExchangeAndIntervalView(mock.get(3).getOpenTime());

    for (OHLCDataImpl each : mock) {
      data.add(new OHLCWithExchangeAndInternalData(Exchange.BINANCE, KlineInterval.m1, each));
    }

    // [157598 9175 466
    // 157598 8995 465 ]

    System.out.println(data.first().getOpenTime() + "  " + data.first().getTimeKey());
    System.out.println(data.last().getOpenTime() + "  " + data.last().getTimeKey());

    System.out.println("\n\n==========ALL===========\n\n");

    for (final OHLCWithExchangeAndIntervalView each : data) {
      System.out.println(each.getOpenTime() + "   " + each.getTimeKey());
    }

    System.out.println("\n\n========Range=============\n\n");

    System.out.println(start.getOpenTime() + "  " + mock.get(1).getTimeKey());
    System.out.println(end.getOpenTime() + "  " + mock.get(3).getTimeKey());

    System.out.println("\n=====================\n");

    final NavigableSet<OHLCWithExchangeAndIntervalView> result =
        data.subSet(start, true, end, true);

    for (OHLCWithExchangeAndIntervalView each : result) {
      System.out.println(each.getOpenTime() + "   " + each.getTimeKey());
    }
  }
}
