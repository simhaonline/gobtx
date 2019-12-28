package com.gobtx.xchange.huobi;

import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.dao.HistoricalDataInitiator;
import com.gobtx.xchange.dao.MarketBatchCutoffTimeFinder;
import com.gobtx.xchange.statistic.BatchLoadStatistic;
import com.huobi.client.AsyncRequestClient;
import com.huobi.client.SubscriptionClient;
import com.huobi.client.model.Candlestick;
import com.huobi.client.model.enums.CandlestickInterval;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.gobtx.xchange.huobi.HuobiMarketDataService.mapInternal;
import static com.gobtx.xchange.logger.DataLogger.appendData;

/** Created by Aaron Kuai on 2019/11/13. */
@SuppressWarnings("Duplicates")
public class HuobiInitiator
    extends HistoricalDataInitiator<
        HuobiMarketDataService, CandlestickInterval, SubscriptionClient> {

  public static final int LIMIT = 2000;

  public HuobiInitiator(@Autowired(required = false) MarketBatchCutoffTimeFinder finder) {
    super(finder, Exchange.HUOBI);
  }

  @Override
  protected CandlestickInterval mapKline(KlineInterval interval) {
    return mapInternal(interval);
  }

  @Override
  protected int limit() {
    return LIMIT;
  }

  protected int size() {
    return 300;
  }

  @Override
  protected Runnable loadJob(
      final String internalSymbol,
      final String externalSymbol,
      final BatchLoadStatistic.IntervalLoadStatistic childStatistic,
      final HuobiMarketDataService service,
      final CandlestickInterval interval,
      final KlineInterval innerInterval,
      final long startTime,
      final CountDownLatch latch,
      final boolean derived) {

    final AsyncRequestClient client = AsyncRequestClient.create();

    // https://api.huobi.pro/market/history/kline?period=1min&size=2000&symbol=btcusdt
    // {
    //      "amount": 5.787900770890335,
    //      "open": 7209.38,
    //      "close": 7211.72,
    //      "high": 7212,
    //      "id": 1576206300,
    //      "count": 65,
    //      "low": 7209.38,
    //      "vol": 41737.1968204607
    //    },
    //    {
    //      "amount": 1.550621551463221,
    //      "open": 7209.24,
    //      "close": 7209.29,
    //      "high": 7210.48,
    //      "id": 1576206240,
    //      "count": 53,
    //      "low": 7209.24,
    //      "vol": 11180.565644849301
    //    }
    final int[] gap =
        new int[] {
          (int) ((System.currentTimeMillis() - startTime) / (innerInterval.getMillis()) + 5)
        };

    if (gap[0] > 2000) {
      gap[0] = 2000;
    }

    logger.warn(
        "TRY_KICK_JOB HUOBI {},{},{},SIZE:  [{}]",
        internalSymbol,
        externalSymbol,
        interval,
        gap[0]);

    // Huobi did not support the time range

    // final CandlestickRequest request =
    //    new CandlestickRequest(externalSymbol, interval, null, null, gap);

    // Stupid SDK will not narrow the 2000
    return () -> {
      final long begin = System.currentTimeMillis();

      client.getLatestCandlestick(
          externalSymbol,
          interval,
          gap[0],
          response -> {
            if (response.succeeded()) {
              final List<Candlestick> candlesticks = response.getData();

              childStatistic.plusBatchSize(candlesticks.size());
              childStatistic.plusQueryCount();

              // Reverse the order
              // DESC

              // Always get the same range
              // [2019-12-11-16:05:44] GOT: [2019-12-16-16:07:00 ~ 2019-12-15-06:48:00]

              // [2019-12-15-06:48:00] GOT: [2019-12-16-16:08:00 ~ 2019-12-15-06:49:00]
              // Huobi's is desc

              if (!candlesticks.isEmpty()) {
                logger.warn(
                    "FETCH_ROUND HUOBI {},{},{} [{}] GOT: [{} ~ {}]",
                    internalSymbol,
                    innerInterval,
                    candlesticks.size(),
                    FORMATTER.format(Instant.ofEpochMilli(startTime)),
                    candlesticks.size() > 0
                        ? FORMATTER.format(Instant.ofEpochMilli(candlesticks.get(0).getId() * 1000))
                        : 0,
                    candlesticks.size() > 0
                        ? FORMATTER.format(
                            Instant.ofEpochMilli(
                                candlesticks.get(candlesticks.size() - 1).getId() * 1000))
                        : 0);

                childStatistic.setLastOpenTime(candlesticks.get(0).getId() * 1000);
                if (childStatistic.getFirstOpenTime() <= 0) {
                  childStatistic.setFirstOpenTime(
                      candlesticks.get(candlesticks.size() - 1).getId() * 1000);
                }

                candlesticks.forEach(
                    input -> {
                      OHLCDataImpl data =
                          new OHLCDataImpl()
                              .setSymbol(internalSymbol)
                              .setOpenTime(input.getId() * 1_000) // As huobi's is second not ms
                              .setAmount(input.getAmount())
                              .setVolume(input.getVolume())
                              .setNumberOfTrades(input.getCount())
                              .setOpen(input.getOpen())
                              .setClose(input.getClose())
                              .setLow(input.getLow())
                              .setHigh(input.getHigh());

                      appendData(data, Exchange.HUOBI, innerInterval);

                      if (!service.listeners().isEmpty()) {
                        service
                            .listeners()
                            .forEach(
                                it ->
                                    it.update(
                                        data,
                                        innerInterval,
                                        Exchange.HUOBI,
                                        internalSymbol,
                                        derived));
                      }
                    });
              } else {

                logger.warn(
                    "FETCH_ROUND_EMPTY HUOBI {},{},{}",
                    internalSymbol,
                    innerInterval,
                    candlesticks.size());
              }

              // if (candlesticks.size() < limit()) {
              // This is the last one then it is done
              latch.countDown();
              childStatistic.setEndTime(System.currentTimeMillis());

              logger.warn(
                  "DONE_FETCH HUOBI {},{},{}, LEFT:{}",
                  internalSymbol,
                  innerInterval,
                  candlesticks.size(),
                  latch.getCount());
              //                } else {
              //                  GlobalScheduleService.INSTANCE.schedule(
              //                      loadJob(
              //                          internalSymbol,
              //                          externalSymbol,
              //                          childStatistic,
              //                          service,
              //                          interval,
              //                          innerInterval,
              //                          childStatistic.getLastOpenTime(),
              //                          latch,
              //                          derived),
              //                      40 * childStatistic.getQueryCount(),
              //                      TimeUnit.SECONDS); // A bit delay avoid the server block us?
              //                }
            } else {
              latch.countDown();
              childStatistic.setEndTime(System.currentTimeMillis());
              logger.error(
                  "FAIL_GET_HISTORY_CANDLE HUOBI {},{},{}, LEFT:{}\n{}\n",
                  internalSymbol,
                  externalSymbol,
                  interval,
                  latch.getCount(),
                  ExceptionUtils.getStackFrames(response.getException()));
            }
          });

      logger.warn(
          "FETCH_ROUND_COST HUOBI {},{} [{}]s",
          internalSymbol,
          innerInterval,
          (System.currentTimeMillis() - begin) / 1_000);
    };
  }

  @Override
  protected int frequentLimit(int cnt) {
    return cnt;
  }
}
