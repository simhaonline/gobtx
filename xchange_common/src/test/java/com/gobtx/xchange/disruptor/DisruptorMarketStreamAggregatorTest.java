package com.gobtx.xchange.disruptor;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.repository.LocalTestMarketDataRepository;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.concurrent.TimeUnit.MINUTES;

/** Created by Aaron Kuai on 2019/11/12. */
public class DisruptorMarketStreamAggregatorTest {

  private DisruptorConfiguration configuration;

  private LocalTestMarketDataRepository repository;

  private DisruptorMarketStreamAggregator aggregator;

  @Before
  public void init() {

    configuration =
        new DisruptorConfiguration()
            .setBufferSize(2 << 12)
            .setProcessParallelCnt(4)
            .setProducerType(ProducerType.MULTI)
            .setWaitStrategy(new BlockingWaitStrategy());

    repository = new LocalTestMarketDataRepository();

    aggregator = new DisruptorMarketStreamAggregator(configuration, repository);
  }

  @Test
  public void start() throws InterruptedException {

    aggregator.start();

    List<OHLCData> mock =
        Arrays.asList(
            new OHLCDataImpl()
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(8))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(7))
                .setOpen(BigDecimal.valueOf(103.10))
                .setHigh(BigDecimal.valueOf(125.88))
                .setLow(BigDecimal.valueOf(120.34))
                .setVolume(BigDecimal.valueOf(2323233))
                .setAmount(BigDecimal.valueOf(2323233))
                .setNumberOfTrades((111))
                .setClose(BigDecimal.valueOf(124.12)),
            new OHLCDataImpl()
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(5))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(4))
                .setOpen(BigDecimal.valueOf(123.11))
                .setHigh(BigDecimal.valueOf(125.88))
                .setLow(BigDecimal.valueOf(120.34))
                .setVolume(BigDecimal.valueOf(2323233))
                .setAmount(BigDecimal.valueOf(2323233))
                .setNumberOfTrades((111))
                .setClose(BigDecimal.valueOf(124.12)),
            new OHLCDataImpl()
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(4))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(3))
                .setOpen(BigDecimal.valueOf(113.11))
                .setHigh(BigDecimal.valueOf(115.88))
                .setLow(BigDecimal.valueOf(110.34))
                .setVolume(BigDecimal.valueOf(3323233))
                .setAmount(BigDecimal.valueOf(2323233))
                .setNumberOfTrades((112))
                .setClose(BigDecimal.valueOf(114.12)),
            new OHLCDataImpl()
                .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(3))
                .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(2))
                .setOpen(BigDecimal.valueOf(133.11))
                .setHigh(BigDecimal.valueOf(135.88))
                .setLow(BigDecimal.valueOf(130.34))
                .setVolume(BigDecimal.valueOf(2823233))
                .setAmount(BigDecimal.valueOf(2323233))
                .setNumberOfTrades((113))
                .setClose(BigDecimal.valueOf(134.12)));

    //    OHLCData data =
    //        new OHLCDataImpl()
    //            .setTimeKey(12332)
    //            .setOpenTime(System.currentTimeMillis())
    //            .setCloseTime(999999999l)
    //            .setOpen(BigDecimal.valueOf(3333333333l))
    //            .setHigh(BigDecimal.valueOf(4444444444l))
    //            .setLow(BigDecimal.valueOf(5555555555l))
    //            .setClose(BigDecimal.valueOf(6666666666l))
    //            .setAmount(BigDecimal.valueOf(777777777l))
    //            .setVolume(BigDecimal.valueOf(8888888888l))
    //            .setNumberOfTrades(BigDecimal.valueOf(9999999999l));

    for (OHLCData data : mock) {
      aggregator.update(data, KlineInterval.m1, Exchange.BINANCE, "btcusdt");
    }

    Thread.sleep(2_000);

    Assert.assertTrue(!repository.cache.isEmpty());

    prettyPrint(repository.cache);
  }

  private void prettyPrint(
      final Map<Exchange, Map<KlineInterval, Map<String, TreeMap<Long, OHLCData>>>> cache) {

    cache.forEach(
        (ex, row) -> {
          System.out.println(ex);

          row.forEach(
              (interval, table) -> {
                System.out.println("\t" + interval);

                table.forEach(
                    (symbol, tree) -> {
                      System.out.println("\t\t" + symbol);

                      tree.forEach(
                          (k, data) -> {
                            System.out.println("\t\t\t\t" + k + " : \t" + data);
                          });
                    });
              });
        });
  }
}
