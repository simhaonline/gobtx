package com.gobtx.xchange.service;

import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.MarketDataStreamListener;
import com.gobtx.xchange.TradeDataStreamListener;
import com.gobtx.xchange.configuration.SymbolMapper;
import com.gobtx.xchange.service.statistic.DummyMarketDataServiceStatistic;
import com.gobtx.xchange.service.statistic.MarketDataServiceStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Created by Aaron Kuai on 2019/11/13. */
public abstract class AbstractMarketDataService implements MarketDataService {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final AtomicBoolean started = new AtomicBoolean(false);

  protected final CopyOnWriteArrayList<MarketDataStreamListener> listeners =
      new CopyOnWriteArrayList<>();

  protected final CopyOnWriteArrayList<TradeDataStreamListener> tradeListeners =
      new CopyOnWriteArrayList<>();

  protected MarketDataServiceStatistic statistic;

  protected SymbolMapper symbolMapper;

  @Override
  public void start() {

    if (started.compareAndSet(false, true)) {

      if (logger.isDebugEnabled()) {
        logger.debug("TRY_START_MKT_SERVICE {},{}", exchange(), version());
      }

      if (statistic == null) {
        statistic = DummyMarketDataServiceStatistic.dummyStatistic(exchange());
      }

      doStart();
    }
  }

  protected final Set<KlineInterval> genericIntervals =
      new LinkedHashSet<KlineInterval>() {
        {
          add(KlineInterval.m1);
          add(KlineInterval.h1);
          add(KlineInterval.d1);
        }
      };

  protected int initDelayInSeconds() {
    return 5;
  }

  protected int reconFrequentInSeconds() {
    return 120;
  }

  protected int reconLimit() {
    return 1;
  }

  protected int reconCutTimestamp() {
    return 0;
  }

  protected void afterStart() {

    int size = 1;
    for (KlineInterval interval : supportReconKlineIntervals()) {

      if (!genericIntervals.contains(interval)) {

        for (final String symbol : initSymbols()) {

          logger.warn("KICK_RECON_FOR {},{},{}", exchange(), symbolMapper.map(symbol), interval);

          GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
              () ->
                  history(interval, symbol, reconLimit(), reconCutTimestamp())
                      .forEach(
                          it -> {
                            // Pub it
                            listeners.forEach(
                                listener ->
                                    listener.update(it, interval, exchange(), it.getSymbol()));
                          }),
              (initDelayInSeconds() * (size++)),
              reconFrequentInSeconds(), // 2 minutes
              TimeUnit.SECONDS);
        }
      }
    }
  }

  protected abstract Collection<String> initSymbols();

  @Override
  public MarketDataServiceStatistic statistic() {
    return statistic;
  }

  protected abstract void doStart();

  @Override
  public void stop() {

    if (started.compareAndSet(true, false)) {
      if (logger.isDebugEnabled()) {
        logger.debug("TRY_STOP_MKT_SERVICE {},{}", exchange(), version());
      }
      doStop();
    }
  }

  protected abstract void doStop();

  @Override
  public Closeable listener(MarketDataStreamListener listener) {
    listeners.add(listener);
    return () -> listeners.remove(listener);
  }

  @Override
  public Closeable tradeListener(TradeDataStreamListener listener) {
    tradeListeners.add(listener);
    return () -> tradeListeners.remove(listener);
  }
}
