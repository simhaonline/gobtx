package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.exception.IllegalOperationException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Created by Aaron Kuai on 2019/11/14. */
public class MarketDataFlusherContext {

  protected static boolean registered = false;

  protected static final Map<String, Flusher> flusherMap = new ConcurrentHashMap<>();

  public static void register(
      final Exchange exchange, final KlineInterval interval, final Flusher flusher) {

    registered = true;
    flusherMap.put(exchange.cacheName(interval), flusher);
  }

  public static void registerAllTest(final Flusher flusher) {
    registered = true;

    for (Exchange exchange : Exchange.values()) {

      for (KlineInterval interval : KlineInterval.VALS) {
        flusherMap.put(exchange.cacheName(interval), flusher);
      }
    }
  }

  public static void registerByExchange(final Exchange exchange, final Flusher flusher) {

    registered = true;
    for (KlineInterval interval : KlineInterval.VALS) {
      flusherMap.put(exchange.cacheName(interval), flusher);
    }
  }

  public static Flusher flusher(final Exchange exchange, final KlineInterval interval) {

    if (!registered) {

      throw new IllegalOperationException("not init yet!");
    }
    return flusherMap.get(exchange.cacheName(interval));
  }
}
