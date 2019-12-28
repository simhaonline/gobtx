package com.gobtx.xchange.logger;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import com.gobtx.model.view.TradeEventView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Aaron Kuai on 2019/11/14. */
public abstract class DataLogger {

  private static final Logger logger = LoggerFactory.getLogger("data");

  @FunctionalInterface
  interface LoggerAppender {
    void append(final OHLCView data, final Exchange exchange, final KlineInterval interval);
  }

  static final LoggerAppender EMPTY = (data, exchange, interval) -> {};

  static final LoggerAppender DEBUG =
      (data, exchange, interval) -> {
        final StringBuffer sb = new StringBuffer();
        sb.append(exchange)
            .append("|")
            .append(data.getSymbol())
            .append("|")
            .append(interval)
            .append("|")
            .append(data.getOpenTime())
            .append("|")
            .append(data.getCloseTime())
            .append("|")
            .append(data.getOpen())
            .append("|")
            .append(data.getHigh())
            .append("|")
            .append(data.getLow())
            .append("|")
            .append(data.getClose())
            .append("|")
            .append(data.getVolume())
            .append("|")
            .append(data.getAmount())
            .append("|")
            .append(data.getNumberOfTrades());

        logger.debug(sb.toString());
      };

  public static final LoggerAppender DATA_APPENDER = logger.isDebugEnabled() ? DEBUG : EMPTY;

  public static void appendData(
      final OHLCView data, final Exchange exchange, final KlineInterval interval) {
    DATA_APPENDER.append(data, exchange, interval);
  }

  // Trade

  private static final Logger tradeLogger = LoggerFactory.getLogger("trade");

  @FunctionalInterface
  interface TradeLoggerAppender {
    void append(final TradeEventView data, final Exchange exchange);
  }

  static final TradeLoggerAppender TRADE_EMPTY = (data, exchange) -> {};

  static final TradeLoggerAppender TRADE_DEBUG =
      (data, exchange) -> {
        final StringBuffer sb = new StringBuffer();

        sb.append(exchange)
            .append("|")
            .append(data.getSymbol())
            .append("|")
            .append(data.getTradeTimestamp())
            .append("|")
            .append(data.getPrice())
            .append("|")
            .append(data.getQuantity());

        tradeLogger.debug(sb.toString());
      };

  public static final TradeLoggerAppender TRADE_APPENDER =
      tradeLogger.isDebugEnabled() ? TRADE_DEBUG : TRADE_EMPTY;

  public static void appendTrade(final TradeEventView data, final Exchange exchange) {
    TRADE_APPENDER.append(data, exchange);
  }
}
