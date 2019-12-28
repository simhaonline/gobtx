package com.gobtx.frontend.ws.netty.session;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Created by Aaron Kuai on 2019/11/19. */
public class ChannelContextMap {

  protected Set<String>[] tickerMap = new Set[Exchange.VALS.length];

  public Set<String>[] getTickerMap() {
    return tickerMap;
  }

  protected Map<String, Set<KlineInterval>>[] klineMap = new Map[Exchange.VALS.length];

  public Map<String, Set<KlineInterval>>[] getKlineMap() {
    return klineMap;
  }

  static final Function<String, Set<KlineInterval>> LEVEL2_FACTORY =
      s -> new HashSet<>(KlineInterval.VALS.length);

  public ChannelContextMap() {

    final int exchangeLength = Exchange.VALS.length;

    for (int i = 0; i < exchangeLength; i++) {
      tickerMap[i] = new HashSet<>();
      klineMap[i] = new ConcurrentHashMap<>();
    }
  }

  public boolean register(final Exchange exchange, final KlineInterval kline, final String symbol) {

    return klineMap[exchange.ordinal()].computeIfAbsent(symbol, LEVEL2_FACTORY).add(kline);
  }

  public boolean registers(
      final Exchange exchange, final KlineInterval kline, final Collection<String> symbols) {

    boolean res = true;
    for (final String symbol : symbols) {

      if (!klineMap[exchange.ordinal()].computeIfAbsent(symbol, LEVEL2_FACTORY).add(kline)) {

        res = false;
      }
    }
    return res;
  }

  public Map<String, Set<KlineInterval>> unregisterByExchange(final Exchange exchange) {
    Map<String, Set<KlineInterval>> res = new HashMap<>(klineMap[exchange.ordinal()]);
    klineMap[exchange.ordinal()].clear();
    return res;
  }

  public Set<KlineInterval> unregisterByExchangeAndSymbol(
      final Exchange exchange, final String symbol) {
    return klineMap[exchange.ordinal()].remove(symbol);
  }

  // Map<Exchange, Map<String, Set<KlineInterval>>>
  public boolean unregister(
      final Exchange exchange, final KlineInterval kline, final String symbol) {

    final Map<String, Set<KlineInterval>> row1 = klineMap[exchange.ordinal()];

    if (row1 != null && !row1.isEmpty()) {

      final Set<KlineInterval> row2 = row1.get(symbol);

      if (row2 != null && !row2.isEmpty()) {
        return row2.remove(kline);
      }
    }
    return false;
  }

  public boolean unregisters(
      final Exchange exchange, final KlineInterval kline, final Collection<String> symbols) {

    boolean res = true;

    for (final String symbol : symbols) {

      if (!unregister(exchange, kline, symbol)) {
        res = false;
      }
    }
    return res;
  }

  public ChannelContextMap clear() {

    for (int i = 0; i < Exchange.VALS.length; i++) {
      tickerMap[i].clear();
      klineMap[i].clear();
      tickerMap[i] = null;
      klineMap[i] = null;
    }
    tickerMap = null;
    klineMap = null;

    return this;
  }

  // -------------------------------------Ticker----------------------------

  public boolean registerTicker(final Exchange exchange, final String symbol) {
    return tickerMap[exchange.ordinal()].add(symbol);
  }

  public boolean registerTickers(final Exchange exchange, final Collection<String> symbols) {

    boolean res = true;
    for (final String symbol : symbols) {

      if (!registerTicker(exchange, symbol)) {
        res = false;
      }
    }
    return res;
  }

  public boolean unregisterTicker(final Exchange exchange, final String symbol) {
    return tickerMap[exchange.ordinal()].remove(symbol);
  }

  public boolean unregisterTickers(final Exchange exchange, final Collection<String> symbols) {

    boolean res = true;

    for (final String symbol : symbols) {
      if (!unregisterTicker(exchange, symbol)) {
        res = false;
      }
    }
    return res;
  }

  public Set<String> unregisterTickerByExchange(final Exchange exchange) {

    final Set<String> res = new HashSet<>(tickerMap[exchange.ordinal()]);
    tickerMap[exchange.ordinal()].clear();
    return res;
  }
}
