package com.gobtx.model.statistic;

import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.enums.Exchange;
import com.huobi.client.AsyncRequestClient;
import com.huobi.client.model.Candlestick;
import com.huobi.client.model.enums.CandlestickInterval;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Created by Aaron Kuai on 2019/12/23. */
public class HuobiTrade24HProvider extends AbstractTrade24HProvider {

  protected Map<String, Trade24HStatistic> cache = new ConcurrentHashMap<>();

  public HuobiTrade24HProvider(SymbolProvider provider) {
    super(provider);
  }

  AsyncRequestClient client;

  @Override
  protected void beforeStart() {
    client = AsyncRequestClient.create();
  }

  AtomicLong cnt = new AtomicLong(0);

  @Override
  protected void afterSuccess(Trade24HStatistic statistic, boolean first) {
    if (first) {
      // Try to schedule
      logger.debug("SCHEDULE_REG_CHECK_OF_24HR  {},{}", exchange(), statistic.getSymbol());

      scheduledFutureMap.computeIfAbsent(
          statistic.getSymbol(),
          k ->
              GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
                  () -> doFetch(statistic.getSymbol(), HuobiTrade24HProvider.this, false),
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
                () -> doFetch(symbol, HuobiTrade24HProvider.this, true), 1, 1, TimeUnit.MINUTES));
  }

  @Override
  protected void doFetch(
      final String symbol, final Trade24HStatistic3rdPartyCallBack callBack, final boolean first) {
    // Huobi's is a bit stupid not sure how to do this?
    // Fetch latest 2 days K line and then aggregate it
    final Runnable task =
        () ->
            client.getLatestCandlestick(
                symbol.toLowerCase(),
                CandlestickInterval.DAY1,
                2,
                response -> {
                  if (response.succeeded()) {
                    List<Candlestick> latest2 = response.getData();
                    // Bigger --> Smaller
                    if (latest2.size() == 2) {

                      // https://api.huobi.pro/market/history/kline?period=1day&size=200&symbol=btcusdt

                      final Candlestick yesterday = latest2.get(1);
                      final Candlestick today = latest2.get(0);

                      final Trade24HStatistic statistic =
                          new Trade24HStatistic(Exchange.HUOBI, symbol.toUpperCase());

                      statistic
                          .setTimestamp(System.currentTimeMillis())
                          .setOpen(today.getOpen())
                          .setHigh(today.getHigh())
                          .setLow(today.getLow())
                          .setClose(today.getClose())
                          .setCount(today.getCount())
                          .setVolume(today.getVolume())
                          .setAmount(today.getAmount())
                          .setPrevClosed(yesterday.getClose());

                      callBack.success(statistic, first);
                    }

                  } else {
                    callBack.fail(symbol, response.getException());
                  }
                });

    if (first) {
      // Try to schedule them as gen
      GlobalScheduleService.INSTANCE.schedule(task, 3 * cnt.incrementAndGet(), TimeUnit.SECONDS);
    } else {
      task.run();
    }
  }

  @Override
  public Exchange exchange() {
    return Exchange.HUOBI;
  }
}
