package com.gobtx.model.statistic;

import com.gobtx.common.executor.GlobalExecutorService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/** Created by Aaron Kuai on 2019/12/23. */
public abstract class AbstractTrade24HProvider
    implements Trade24HProvider, Trade24HStatistic3rdPartyCallBack {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final List<Trade24HProviderListener> listeners = new CopyOnWriteArrayList<>();

  protected final AtomicBoolean started = new AtomicBoolean(false);

  protected final SymbolProvider provider;

  protected final Map<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();

  public AbstractTrade24HProvider(SymbolProvider provider) {
    this.provider = provider;
  }

  @Override
  public Closeable listener(Trade24HProviderListener listener) {
    listeners.add(listener);
    return () -> listeners.remove(listener);
  }

  @Override
  public void start() {
    if (started.compareAndSet(false, true)) {
      logger.debug("TRY_START_24HR_STATISTIC {}", exchange());
      beforeStart();
      for (final String symbol : provider.symbols(exchange())) {
        symbol.intern();
        logger.debug("TRY_START_24HR_STATISTIC_OF_SYMBOL {},{}", exchange(), symbol);
        GlobalExecutorService.INSTANCE.submit(
            () -> doFetch(symbol, AbstractTrade24HProvider.this, true));
      }
      afterStart();
    }
  }

  protected abstract void doFetch(
      final String symbol, final Trade24HStatistic3rdPartyCallBack callBack, final boolean first);

  protected void beforeStart() {};

  protected void afterStart() {};

  @Override
  public void stop() {

    if (!scheduledFutureMap.isEmpty()) {

      scheduledFutureMap.forEach(
          (k, scheduledFuture) -> {
            try {
              scheduledFuture.cancel(true);
            } catch (Throwable ex) {
            }
          });

      scheduledFutureMap.clear();
    }
  }

  @Override
  public void success(final Trade24HStatistic statistic, final boolean first) {
    listeners.forEach(
        it -> {
          it.update(statistic);
        });
    afterSuccess(statistic, first);
  }

  @Override
  public void fail(String symbol, Throwable throwable) {

    logger.error(
        "FAIL_FETCH_STATISTIC  {},{}", exchange(), ExceptionUtils.getStackTrace(throwable));

    ScheduledFuture scheduledFuture = scheduledFutureMap.remove(symbol);
    if (scheduledFuture != null) {
      try {
        scheduledFuture.cancel(true);
      } catch (Throwable ex) {
      }
    }
    afterFail(symbol, throwable);
  }

  protected void afterSuccess(final Trade24HStatistic statistic, final boolean first) {};

  protected void afterFail(String symbol, Throwable throwable) {};
}
