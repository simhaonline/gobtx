package com.gobtx.xchange.dao;

import com.gobtx.common.executor.GlobalExecutorService;
import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.enums.KlineIntervalGroup;
import com.gobtx.xchange.LoadMode;
import com.gobtx.xchange.configuration.SymbolMapper;
import com.gobtx.xchange.service.MarketDataService;
import com.gobtx.xchange.statistic.BatchLoadStatistic;
import com.gobtx.xchange.statistic.InitiatorCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.gobtx.model.enums.KlineInterval.*;

/** Created by Aaron Kuai on 2019/12/13. */
public abstract class HistoricalDataInitiator<T extends MarketDataService, KT, CS> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final MarketBatchCutoffTimeFinder finder;

  protected final Exchange exchange;

  protected T service;

  protected static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));

  public HistoricalDataInitiator<T, KT, CS> setService(T service) {
    this.service = service;
    return this;
  }

  protected HistoricalDataInitiator(MarketBatchCutoffTimeFinder finder, Exchange exchange) {
    this.finder = finder == null ? AlwaysFromVeryBeginMarketBatchCutoffTimeFinder.INSTANCE : finder;
    this.exchange = exchange;
  }

  public void sync(
      final Collection<String> symbols,
      final SymbolMapper mapper,
      final T service,
      final CS connectionService,
      final InitiatorCallback callback) {

    logger.warn("TRY_CATCH_UP_MARKET_DATA_GAP {}", exchange);

    setService(service);

    final List<KlineIntervalGroup> groups =
        KlineIntervalGroup.group(service.supportKlineIntervals());

    CountDownLatch latch;

    final BatchLoadStatistic statistic = new BatchLoadStatistic(exchange);

    if (LoadMode.init().isHistory()) {
      latch = doHistoryLoad(symbols, mapper, service, connectionService, groups, statistic);
    } else {
      latch = doIncrementalLoad(symbols, mapper, service, connectionService, statistic);
    }

    GlobalExecutorService.INSTANCE.submit(
        () -> {
          try {
            latch.await(timeoutInMinutes(), TimeUnit.MINUTES);
            statistic.setEndTime(System.currentTimeMillis());
            callback.done(statistic, true);
          } catch (Throwable e) {
            logger.error("FAIL_WAIT_BATCH_JOB_DONE {},{}", exchange, e);
            statistic.setEndTime(System.currentTimeMillis());
            callback.done(statistic, false);
          }
        });
  }

  protected CountDownLatch doHistoryLoad(
      final Collection<String> symbols,
      final SymbolMapper mapper,
      final T service,
      final CS connectionService,
      final List<KlineIntervalGroup> groups,
      final BatchLoadStatistic statistic) {

    int loadCnt = 0;
    for (final KlineIntervalGroup group : groups) {
      loadCnt += (group.getStandalone().size() + 1);
    }
    loadCnt *= symbols.size();

    logger.warn("CATCH_DATA_UNDER_HISTORY_MODE {} -- {}", exchange, loadCnt);

    final CountDownLatch latch = new CountDownLatch(loadCnt);

    int cnt = 0;
    for (final KlineIntervalGroup group : groups) {

      final KT interval = mapKline(group.getInterval());
      final KlineInterval innerInterval = group.getInterval();

      final long startTime = System.currentTimeMillis() - innerInterval.getHistoryLength();

      for (final String externalSymbol : symbols) {

        final String internalSymbol = mapper.map(externalSymbol);

        logger.warn(
            "TRY_HIS_LOAD {} {},{},{},{}",
            exchange,
            internalSymbol,
            externalSymbol.toUpperCase(),
            interval,
            FORMATTER.format(Instant.ofEpochMilli(startTime)));

        final BatchLoadStatistic.IntervalLoadStatistic childStatistic =
            statistic.statistic(group.getInterval(), internalSymbol).setCutTime(startTime);

        int delay = frequentLimit(cnt++);
        if (delay <= 0) {
          GlobalExecutorService.INSTANCE.submit(
              loadJob(
                  internalSymbol,
                  externalSymbol,
                  childStatistic,
                  service,
                  interval,
                  group.getInterval(),
                  startTime,
                  latch,
                  false));
        } else {

          logger.warn(
              "DELAY_ROUND {},{},{},{}", exchange, group.getInterval(), internalSymbol, delay);

          GlobalScheduleService.INSTANCE.schedule(
              loadJob(
                  internalSymbol,
                  externalSymbol,
                  childStatistic,
                  service,
                  interval,
                  group.getInterval(),
                  startTime,
                  latch,
                  false),
              delay,
              TimeUnit.SECONDS);
        }

        if (!group.getStandalone().isEmpty()) {
          // issue each's

          for (KlineInterval internalKline : group.getStandalone()) {
            final KT chInterval = mapKline(internalKline);

            final long startTime2 = System.currentTimeMillis() - internalKline.getHistoryLength();

            final BatchLoadStatistic.IntervalLoadStatistic childStatistic2 =
                statistic.statistic(internalKline, internalSymbol).setCutTime(startTime2);

            logger.warn(
                "TRY_HIS_LOAD {} {},{},{},{}",
                exchange,
                internalSymbol,
                externalSymbol.toUpperCase(),
                internalKline,
                FORMATTER.format(Instant.ofEpochMilli(startTime2)));

            delay = frequentLimit(cnt++);

            if (delay <= 0) {
              GlobalExecutorService.INSTANCE.submit(
                  loadJob(
                      internalSymbol,
                      externalSymbol,
                      childStatistic2,
                      service,
                      chInterval,
                      internalKline,
                      startTime2,
                      latch,
                      false));
            } else {
              logger.warn(
                  "DELAY_ROUND {},{},{},{}", exchange, internalKline, internalSymbol, delay);
              GlobalScheduleService.INSTANCE.schedule(
                  loadJob(
                      internalSymbol,
                      externalSymbol,
                      childStatistic2,
                      service,
                      chInterval,
                      internalKline,
                      startTime2,
                      latch,
                      false),
                  delay,
                  TimeUnit.SECONDS);
            }
          }
        }
      }
    }
    return latch;
  }

  protected CountDownLatch doIncrementalLoad(
      final Collection<String> symbols,
      final SymbolMapper mapper,
      final T service,
      final CS connectionService,
      final BatchLoadStatistic statistic) {

    logger.warn("CATCH_DATA_UNDER_INCREMENTAL_MODE {}", exchange);

    CountDownLatch latch = new CountDownLatch(symbols.size() * 3);

    for (final String externalSymbol : symbols) {
      final String internalSymbol = mapper.map(externalSymbol);

      logger.warn("KICK_INCREMENTAL {},{},{}", exchange, externalSymbol, internalSymbol);

      for (final KlineInterval internalInterval : new KlineInterval[] {m1, h1, d1}) {

        long startTime = finder.cutoffTime(exchange, internalInterval, internalSymbol);

        startTime =
            startTime <= 0
                ? System.currentTimeMillis() - internalInterval.getHistoryLength()
                : startTime;

        final BatchLoadStatistic.IntervalLoadStatistic childStatistic =
            statistic.statistic(internalInterval, internalSymbol).setCutTime(startTime);

        GlobalExecutorService.INSTANCE.submit(
            loadJob(
                internalSymbol,
                externalSymbol,
                childStatistic,
                service,
                mapKline(internalInterval),
                internalInterval,
                startTime,
                latch,
                true));
      }
    }
    return latch;
  }

  protected abstract KT mapKline(final KlineInterval interval);

  protected abstract int limit();

  protected abstract Runnable loadJob(
      final String internalSymbol,
      final String externalSymbol,
      final BatchLoadStatistic.IntervalLoadStatistic childStatistic,
      final T service,
      final KT interval,
      final KlineInterval innerInterval,
      final long startTime,
      final CountDownLatch latch,
      final boolean derived);

  protected int timeoutInMinutes() {
    return 20;
  }

  protected int frequentLimit(int cnt) {
    return 0;
  }
}
