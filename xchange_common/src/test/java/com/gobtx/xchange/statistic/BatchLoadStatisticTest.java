package com.gobtx.xchange.statistic;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import org.junit.Test;

/** Created by Aaron Kuai on 2019/12/12. */
public class BatchLoadStatisticTest {

  @Test
  public void testToString() {

    BatchLoadStatistic statistic = new BatchLoadStatistic(Exchange.BINANCE);

    statistic
        .statistic(KlineInterval.m1, "BTCUSDT")
        .setEndTime(System.currentTimeMillis() + 5000)
        .setFirstOpenTime(System.currentTimeMillis() - 200_000)
        .setLastOpenTime(System.currentTimeMillis() - 5_000)
        .setBatchSize(2312)
        .setQueryCount(12)
        .setCutTime(System.currentTimeMillis() - 201_000);

    statistic.setEndTime(System.currentTimeMillis() + 200_0000);

    System.out.println(statistic);
  }
}
