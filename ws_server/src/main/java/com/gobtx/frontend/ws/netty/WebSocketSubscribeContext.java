package com.gobtx.frontend.ws.netty;

import com.gobtx.frontend.ws.netty.session.ChannelContextMap;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.gobtx.frontend.ws.netty.Constants.CTX_MAP_ATTR_KEY;

/** Created by Aaron Kuai on 2019/11/19. */
@SuppressWarnings("Duplicates")
public class WebSocketSubscribeContext {

  static final Logger logger = LoggerFactory.getLogger(WebSocketSubscribeContext.class);

  protected static final WebSocketSubscribeContext INSTANCE = new WebSocketSubscribeContext();

  public static WebSocketSubscribeContext getInstance() {
    return INSTANCE;
  }

  private WebSocketSubscribeContext() {

    final int exchangeLength = Exchange.VALS.length;

    for (int i = 0; i < exchangeLength; i++) {

      tickerMap[i] = new ConcurrentHashMap<>(32);
      klineMap[i] = new ConcurrentHashMap<>(32);
    }
  }

  // --- Ticker subscribe
  final Map<String, Map<ChannelId, ChannelHandlerContext>>[] tickerMap =
      new Map[Exchange.VALS.length];

  static final Function<String, Map<ChannelId, ChannelHandlerContext>> TICKER_MAP_FACTORY =
      symbol -> new ConcurrentHashMap<>(512);

  // --- Kline subscribe
  final Map<String, Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>>>[] klineMap =
      new Map[Exchange.VALS.length];

  static final Function<String, Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>>>
      KLINE_MAP_LEVEL1_FACTORY = symbol -> new ConcurrentHashMap<>(KlineInterval.VALS.length);

  static final Function<KlineInterval, Map<ChannelId, ChannelHandlerContext>>
      KLINE_MAP_LEVEL2_FACTORY = interval -> new ConcurrentHashMap<>(512);

  // --- Actions

  private ChannelContextMap ctxMapperFetcher(final ChannelHandlerContext ctx) {
    ChannelContextMap ctxHolder = ctx.channel().attr(CTX_MAP_ATTR_KEY).get();

    if (ctxHolder == null) {

      ctxHolder = new ChannelContextMap();

      if (!ctx.channel().attr(CTX_MAP_ATTR_KEY).compareAndSet(null, ctxHolder)) {
        ctxHolder = ctx.channel().attr(CTX_MAP_ATTR_KEY).get();
      }
    }
    return ctxHolder;
  }

  public void registerTicker(
      final Exchange exchange, final String symbol, final ChannelHandlerContext ctx) {

    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    // do this firstly may not suit but this much safe
    ctxHolder.registerTicker(exchange, symbol);

    tickerMap[exchange.ordinal()]
        .computeIfAbsent(symbol, TICKER_MAP_FACTORY)
        .put(ctx.channel().id(), ctx);
  }

  public void unregisterTicker(
      final Exchange exchange, final String symbol, final ChannelHandlerContext ctx) {

    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    tickerMap[exchange.ordinal()]
        .getOrDefault(symbol, Collections.EMPTY_MAP)
        .remove(ctx.channel().id());

    ctxHolder.unregisterTicker(exchange, symbol);
  }

  public void registerTickers(
      final Exchange exchange, final Collection<String> symbols, final ChannelHandlerContext ctx) {

    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    ctxHolder.registerTickers(exchange, symbols);

    for (final String symbol : symbols) {
      tickerMap[exchange.ordinal()]
          .computeIfAbsent(symbol, TICKER_MAP_FACTORY)
          .put(ctx.channel().id(), ctx);
    }
  }

  public void unregisterTickers(
      final Exchange exchange, final Collection<String> symbols, final ChannelHandlerContext ctx) {
    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    for (final String symbol : symbols) {
      tickerMap[exchange.ordinal()]
          .getOrDefault(symbol, Collections.EMPTY_MAP)
          .remove(ctx.channel().id());
    }
    ctxHolder.unregisterTickers(exchange, symbols);
  }

  public void registerKline(
      final Exchange exchange,
      final KlineInterval interval,
      final String symbol,
      final ChannelHandlerContext ctx) {

    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    ctxHolder.register(exchange, interval, symbol);

    klineMap[exchange.ordinal()]
        .computeIfAbsent(symbol, KLINE_MAP_LEVEL1_FACTORY)
        .computeIfAbsent(interval, KLINE_MAP_LEVEL2_FACTORY)
        .put(ctx.channel().id(), ctx);
  }

  @SuppressWarnings("Duplicates")
  public void unregisterKline(
      final Exchange exchange,
      final KlineInterval interval,
      final String symbol,
      final ChannelHandlerContext ctx) {
    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    final Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>> row1 =
        klineMap[exchange.ordinal()].get(symbol);
    if (row1 != null && !row1.isEmpty()) {

      Map<ChannelId, ChannelHandlerContext> row2 = row1.get(interval);
      if (row2 != null && !row2.isEmpty()) {
        row2.remove(ctx.channel().id());
      }
    }

    ctxHolder.unregister(exchange, interval, symbol);
  }

  public void registerKlines(
      final Exchange exchange,
      final KlineInterval interval,
      final Collection<String> symbols,
      final ChannelHandlerContext ctx) {

    if (logger.isDebugEnabled()) {
      logger.debug("START_HOOK_K_LINES {},{}", symbols, interval);
    }
    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    ctxHolder.registers(exchange, interval, symbols);

    for (final String symbol : symbols) {
      klineMap[exchange.ordinal()]
          .computeIfAbsent(symbol, KLINE_MAP_LEVEL1_FACTORY)
          .computeIfAbsent(interval, KLINE_MAP_LEVEL2_FACTORY)
          .put(ctx.channel().id(), ctx);
    }
  }

  @SuppressWarnings("Duplicates")
  public void unregisterKlines(
      final Exchange exchange,
      final KlineInterval interval,
      final Collection<String> symbols,
      final ChannelHandlerContext ctx) {

    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    for (final String symbol : symbols) {
      final Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>> row1 =
          klineMap[exchange.ordinal()].get(symbol);
      if (row1 != null && !row1.isEmpty()) {

        Map<ChannelId, ChannelHandlerContext> row2 = row1.get(interval);
        if (row2 != null && !row2.isEmpty()) {
          row2.remove(ctx.channel().id());
        }
      }
    }

    ctxHolder.unregisters(exchange, interval, symbols);
  }

  public void unregisterByExchangeAndSymbol(
      final Exchange exchange, final String symbol, final ChannelHandlerContext ctx) {

    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);
    final Set<KlineInterval> intervals = ctxHolder.unregisterByExchangeAndSymbol(exchange, symbol);
    if (intervals != null && !intervals.isEmpty()) {

      final Map<String, Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>>> row =
          klineMap[exchange.ordinal()];

      final Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>> row2 = row.get(symbol);

      if (row2 != null && !row2.isEmpty()) {

        for (final KlineInterval interval : intervals) {
          row2.getOrDefault(interval, Collections.EMPTY_MAP).remove(ctx.channel().id());
        }
      }
    }
  }

  public void unregisterKlineByExchangeAndSymbols(
      final Exchange exchange, final Collection<String> symbols, final ChannelHandlerContext ctx) {
    for (final String symbol : symbols) {
      unregisterByExchangeAndSymbol(exchange, symbol, ctx);
    }
  }

  public void unregisterKlineByExchange(final Exchange exchange, final ChannelHandlerContext ctx) {

    final ChannelContextMap ctxHolder = ctxMapperFetcher(ctx);

    final Map<String, Set<KlineInterval>> removed = ctxHolder.unregisterByExchange(exchange);

    final Map<String, Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>>> row =
        klineMap[exchange.ordinal()];

    if (!removed.isEmpty()) {

      removed.forEach(
          (symbol, intervals) -> {
            final Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>> row2 = row.get(symbol);
            if (row2 != null && !row2.isEmpty()) {

              for (final KlineInterval interval : intervals) {
                row2.getOrDefault(interval, Collections.EMPTY_MAP).remove(ctx.channel().id());
              }
            }
          });
    }
  }

  /**
   * This is special action after the user offline meaning the channel is closed
   *
   * @param ctx
   */
  protected void offLine(final ChannelHandlerContext ctx) {

    final ChannelContextMap newHolder = ctx.channel().attr(CTX_MAP_ATTR_KEY).getAndSet(null);

    if (newHolder == null) {
      logger.warn("CUSTOMER_CTX_MAP_IS_EMPTY {}", ctx.channel());
    } else {

      if (logger.isDebugEnabled()) {
        logger.warn("CLIENT_OFF_LINE_PURGE_DATA {}", ctx.channel());
      }

      for (final Exchange exchange : Exchange.VALS) {

        final Set<String> symbols = newHolder.getTickerMap()[exchange.ordinal()];
        if (symbols != null && !symbols.isEmpty()) {

          if (logger.isDebugEnabled()) {
            logger.debug("CLIENT_OFF_LINE_REMOVE_TICKER {}", symbols);
          }

          final Map<String, Map<ChannelId, ChannelHandlerContext>> target =
              tickerMap[exchange.ordinal()];
          for (final String symbol : symbols) {
            target.getOrDefault(symbol, Collections.EMPTY_MAP).remove(ctx.channel().id());
          }
        }

        final Map<String, Set<KlineInterval>> klines = newHolder.getKlineMap()[exchange.ordinal()];

        if (klines != null && !klines.isEmpty()) {

          final Map<String, Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>>> target =
              klineMap[exchange.ordinal()];

          if (logger.isDebugEnabled()) {
            logger.debug("CLIENT_OFF_LINE_REMOVE_KLINE {}", klines.keySet());
          }

          klines.forEach(
              (symbol, intervals) -> {
                final Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>> row =
                    target.get(symbol);

                if (row != null && !row.isEmpty()) {

                  final ChannelId id = ctx.channel().id();

                  for (final KlineInterval interval : intervals) {
                    row.getOrDefault(interval, Collections.EMPTY_MAP).remove(id);
                  }
                }
              });
        }
      }
      newHolder.clear();
    }
  }

  // --------------Env issue -----------------

  // 1. how to get the client context

  public Collection<ChannelHandlerContext> tickClients(
      final Exchange exchange, final String symbol) {
    return tickerMap[exchange.ordinal()].getOrDefault(symbol, Collections.EMPTY_MAP).values();
  }

  public Collection<ChannelHandlerContext> klineClients(
      final Exchange exchange, final KlineInterval klineInterval, final String symbol) {

    final Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>> row =
        klineMap[exchange.ordinal()].get(symbol);

    if (row == null || row.isEmpty()) return Collections.EMPTY_LIST;

    return row.getOrDefault(klineInterval, Collections.EMPTY_MAP).values();
  }

  public Map<String, Map<KlineInterval, Map<ChannelId, ChannelHandlerContext>>>[] getKlineMap() {
    return klineMap;
  }
}
