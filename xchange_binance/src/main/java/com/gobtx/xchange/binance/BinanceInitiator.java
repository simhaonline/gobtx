package com.gobtx.xchange.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.dao.HistoricalDataInitiator;
import com.gobtx.xchange.dao.MarketBatchCutoffTimeFinder;
import com.gobtx.xchange.statistic.BatchLoadStatistic;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.gobtx.xchange.logger.DataLogger.appendData;

@Component
public class BinanceInitiator
    extends HistoricalDataInitiator<
        BinanceMarketDataService, CandlestickInterval, BinanceApiAsyncRestClient> {

  private static final int LIMIT = 1000;
  // Synchronize the gap between the previous day or etc

  // "rateLimits": [
  //    {
  //      "rateLimitType": "REQUEST_WEIGHT",
  //      "interval": "MINUTE",
  //      "intervalNum": 1,
  //      "limit": 1200
  //    },
  //    {
  //      "rateLimitType": "ORDERS",
  //      "interval": "SECOND",
  //      "intervalNum": 10,
  //      "limit": 100
  //    },
  //    {
  //      "rateLimitType": "ORDERS",
  //      "interval": "DAY",
  //      "intervalNum": 1,
  //      "limit": 200000
  //    }
  //  ]

  public BinanceInitiator(@Autowired(required = false) MarketBatchCutoffTimeFinder finder) {
    super(finder, Exchange.BINANCE);
  }

  @Override
  protected CandlestickInterval mapKline(KlineInterval interval) {
    return service.mapKline(interval);
  }

  @Override
  protected int limit() {
    return 1000;
  }

  @Override
  protected Runnable loadJob(
      final String internalSymbol,
      final String externalSymbol,
      final BatchLoadStatistic.IntervalLoadStatistic childStatistic,
      final BinanceMarketDataService service,
      final CandlestickInterval interval,
      final KlineInterval innerInterval,
      final long startTime,
      final CountDownLatch latch,
      final boolean derived) {

    final BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    final BinanceApiAsyncRestClient client = factory.newAsyncRestClient();

    logger.warn(
        "TRY_KICK_JOB BINANCE {},{},{},{}",
        internalSymbol,
        externalSymbol.toUpperCase(),
        interval,
        FORMATTER.format(Instant.ofEpochMilli(startTime)));

    final boolean isBCT = "BCHUSDT".equals(internalSymbol);

    return () -> {
      long begin = System.currentTimeMillis();
      client.getCandlestickBars(
          externalSymbol.toUpperCase(),
          interval,
          LIMIT,
          // Binance has bug can not fetch
          // FETCH_ROUND BINANCE BCHUSDT,m1,0 [2019-12-14-13:41:40 ~ 0]
          isBCT ? null : startTime,
          null,
          new BinanceApiCallback<List<Candlestick>>() {
            @Override
            public void onResponse(List<Candlestick> response) {

              childStatistic.plusBatchSize(response.size());
              childStatistic.plusQueryCount();

              logger.warn(
                  "FETCH_ROUND BINANCE {},{},{} [{} ~ {}]",
                  internalSymbol,
                  innerInterval,
                  response.size(),
                  FORMATTER.format(Instant.ofEpochMilli(startTime)),
                  response.size() > 0
                      ? FORMATTER.format(
                          Instant.ofEpochMilli(response.get(response.size() - 1).getOpenTime()))
                      : 0);

              response.forEach(
                  candlestick -> {
                    final OHLCDataImpl data =
                        new OHLCDataImpl()
                            .setSymbol(internalSymbol)
                            .setOpenTime(candlestick.getOpenTime())
                            .setCloseTime(candlestick.getCloseTime())
                            .setOpen(new BigDecimal(candlestick.getOpen()))
                            .setHigh(new BigDecimal(candlestick.getHigh()))
                            .setLow(new BigDecimal(candlestick.getLow()))
                            .setClose(new BigDecimal(candlestick.getClose()))
                            .setVolume(new BigDecimal(candlestick.getVolume()))
                            .setAmount(new BigDecimal(candlestick.getQuoteAssetVolume()))
                            .setNumberOfTrades(candlestick.getNumberOfTrades());

                    childStatistic.setLastOpenTime(data.getOpenTime());
                    if (childStatistic.getFirstOpenTime() <= 0) {
                      childStatistic.setFirstOpenTime(data.getOpenTime());
                    }

                    appendData(data, Exchange.BINANCE, innerInterval);

                    if (!service.listeners().isEmpty()) {
                      service
                          .listeners()
                          .forEach(
                              it ->
                                  it.update(
                                      data,
                                      innerInterval,
                                      Exchange.BINANCE,
                                      internalSymbol,
                                      derived));
                    }
                  });

              if (response.size() < LIMIT || isBCT) {
                // This is the last one so it is Done
                // Update to the statistic
                latch.countDown();
                childStatistic.setEndTime(System.currentTimeMillis());

                logger.warn(
                    "DONE_FETCH BINANCE {},{},{}, LEFT:{}",
                    internalSymbol,
                    innerInterval,
                    response.size(),
                    latch.getCount());

              } else {
                // Still cut from the last things go one and go one util the latest
                // Kick another job to fetch
                GlobalScheduleService.INSTANCE.schedule(
                    loadJob(
                        internalSymbol,
                        externalSymbol,
                        childStatistic,
                        service,
                        interval,
                        innerInterval,
                        childStatistic.getLastOpenTime(),
                        latch,
                        derived),
                    2 * childStatistic.getQueryCount(),
                    TimeUnit.SECONDS); // A bit delay avoid the server block us?
              }
            }

            @Override
            public void onFailure(Throwable cause) {
              latch.countDown();
              childStatistic.setEndTime(System.currentTimeMillis());
              logger.error(
                  "FAIL_GET_HISTORY_CANDLE BINANCE {},{},{}, LEFT:{}\n{}\n",
                  internalSymbol,
                  externalSymbol,
                  interval,
                  latch.getCount(),
                  ExceptionUtils.getStackFrames(cause));
            }
          });

      logger.warn(
          "FETCH_ROUND_COST BINANCE {},{} [{}]s",
          internalSymbol,
          innerInterval,
          (System.currentTimeMillis() - begin) / 1_000);
    };
  }
  //// https://api.binance.com/api/v1/klines?symbol=BTCUSDT&interval=1m&limit=1000
  //
  //      // ASC  small-> bigger
  //      // [
  //      //  [
  //      //    1499040000000,      // Open time
  //      //    "0.01634790",       // Open
  //      //    "0.80000000",       // High
  //      //    "0.01575800",       // Low
  //      //    "0.01577100",       // Close
  //      //    "148976.11427815",  // Volume
  //      //    1499644799999,      // Close time
  //      //    "2434.19055334",    // Quote asset volume
  //      //    308,                // Number of trades
  //      //    "1756.87402397",    // Taker buy base asset volume
  //      //    "28.46694368",      // Taker buy quote asset volume
  //      //    "17928899.62484339" // Ignore.
  //      //  ]
  //      // ]
}
