package com.gobtx.model.statistic;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.enums.Exchange;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aaron Kuai on 2019/12/23.
 *
 * <p>This is On fly to check the Binance's 24 HR trading statistic
 */
public class BinanceTrade24HProvider extends AbstractTrade24HProvider {

  public BinanceTrade24HProvider(SymbolProvider provider) {
    super(provider);
  }

  BinanceApiAsyncRestClient client;

  @Override
  protected void beforeStart() {
    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    client = factory.newAsyncRestClient();
  }

  @Override
  protected void afterSuccess(Trade24HStatistic statistic, boolean first) {

    if (first) {
      logger.debug("SCHEDULE_REG_CHECK_OF_24HR  {},{}", exchange(), statistic.getSymbol());

      scheduledFutureMap.computeIfAbsent(
          statistic.getSymbol(),
          k ->
              GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
                  () -> doFetch(statistic.getSymbol(), BinanceTrade24HProvider.this, false),
                  1,
                  1,
                  TimeUnit.MINUTES));
    }
  }

  @Override
  protected void afterFail(String symbol, Throwable throwable) {
    scheduledFutureMap.computeIfAbsent(
        symbol,
        k ->
            GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
                () -> doFetch(symbol, BinanceTrade24HProvider.this, true), 1, 1, TimeUnit.MINUTES));
  }

  private static final String BCH = "BCHABCUSDT";

  private static final String BCH_MAPPED = "BCHUSDT";

  public static String map(final String symbol) {

    switch (symbol) {
      case BCH:
        return BCH_MAPPED;
      default:
        return symbol;
    }
  }

  protected void doFetch(
      final String symbol, final Trade24HStatistic3rdPartyCallBack callBack, final boolean first) {

    // TickerStatistics tickerStatistics = client.get24HrPriceStatistics("NEOETH");

    // https://api.binance.com/api/v1/klines?symbol=BTCUSDT&interval=1d&limit=2
    // Smaller--> Bigger
    // [
    //    [
    //        1577059200000,
    //        "7500.71000000",
    //        "7695.38000000",
    //        "7265.84000000",
    //        "7317.09000000",
    //        "68051.99720300",
    //        1577145599999,
    //        "510710431.76686034",
    //        530669,
    //        "33444.24377200",
    //        "251116788.72529315",
    //        "0"
    //    ],
    //    [
    //        1577145600000,
    //        "7317.30000000",
    //        "7325.59000000",
    //        "7285.00000000",
    //        "7304.74000000",
    //        "1911.82679300",
    //        1577231999999,
    //        "13971712.12610448",
    //        17358,
    //        "987.95033100",
    //        "7220865.50407279",
    //        "0"
    //    ]
    // ]
    client.getCandlestickBars(
        symbol.toUpperCase(),
        CandlestickInterval.DAILY,
        2,
        null,
        null,
        new BinanceApiCallback<List<Candlestick>>() {
          @Override
          public void onResponse(List<Candlestick> response) {

            if (response.size() == 2) {

              final Candlestick yesterday = response.get(0);
              final Candlestick today = response.get(1);

              final Trade24HStatistic statistic =
                  new Trade24HStatistic(Exchange.BINANCE, map(symbol.toUpperCase()));

              statistic
                  .setTimestamp(today.getCloseTime())
                  .setOpen(new BigDecimal(today.getOpen()))
                  .setHigh(new BigDecimal(today.getHigh()))
                  .setLow(new BigDecimal(today.getLow()))
                  .setClose(new BigDecimal(today.getClose()))
                  .setCount(today.getNumberOfTrades())
                  .setVolume(new BigDecimal(today.getQuoteAssetVolume()))
                  .setPrevClosed(new BigDecimal(yesterday.getClose()));

              callBack.success(statistic, first);
            } else {
              callBack.fail(symbol, new Exception("No more than 2 days candle data"));
            }
          }

          @Override
          public void onFailure(Throwable cause) {
            callBack.fail(symbol, cause);
          }
        });

    //    client.get24HrPriceStatistics(
    //        symbol.toUpperCase(),
    //        new BinanceApiCallback<TickerStatistics>() {
    //          @Override
    //          public void onResponse(TickerStatistics response) {
    //            final Trade24HStatistic statistic =
    //                new Trade24HStatistic(Exchange.BINANCE, map(symbol.toUpperCase()));
    //
    //            final double ap = Double.parseDouble(response.getAskPrice());
    //            final double bp = Double.parseDouble(response.getBidPrice());
    //
    //            if (logger.isDebugEnabled()) {
    //              logger.debug(
    //                  "GOT_24HR_STATISTIC BINANCE {},{},{},{},{},{}",
    //                  statistic.getSymbol(),
    //                  response.getOpenPrice(),
    //                  response.getHighPrice(),
    //                  response.getLowPrice(),
    //                  response.getAskPrice(),
    //                  response.getBidPrice());
    //            }
    //
    //            statistic
    //                .setTimestamp(System.currentTimeMillis())
    //                .setOpen(new BigDecimal(response.getOpenPrice()))
    //                .setHigh(new BigDecimal(response.getHighPrice()))
    //                .setLow(new BigDecimal(response.getLowPrice()))
    //                .setClose(
    //                    new BigDecimal((ap + bp) / 2).setScale(statistic.getOpen().scale(),
    // ROUND_UP))
    //                .setCount(response.getCount())
    //                .setVolume(new BigDecimal(response.getVolume()))
    //                .setPrevClosed(new BigDecimal(response.getPrevClosePrice()));
    //
    //            callBack.success(statistic, first);
    //          }
    //
    //          @Override
    //          public void onFailure(Throwable cause) {
    //            callBack.fail(symbol, cause);
    //          }
    //        });
  }

  @Override
  public Exchange exchange() {
    return Exchange.BINANCE;
  }

  //  long[] scale =
  //      new long[] {
  //        /*00*/ 1,
  //        /*01*/ 10,
  //        /*02*/ 100,
  //        /*03*/ 1000,
  //        /*04*/ 10000,
  //        /*05*/ 100000,
  //        /*06*/ 1000000,
  //        /*07*/ 10000000,
  //        /*08*/ 100000000,
  //        /*09*/ 1000000000,
  //        /*10*/ 10000000000l,
  //        /*11*/ 100000000000l,
  //        /*12*/ 1000000000000l,
  //        /*13*/ 10000000000000l,
  //        /*14*/ 100000000000000l,
  //        /*15*/ 1000000000000000l,
  //        /*16*/ 10000000000000000l,
  //        /*17*/ 100000000000000000l,
  //        /*18*/ 1000000000000000000l,
  //      };
}
