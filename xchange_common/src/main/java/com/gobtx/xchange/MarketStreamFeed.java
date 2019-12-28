package com.gobtx.xchange;

/** Created by Aaron Kuai on 2019/11/11. */
public interface MarketStreamFeed {

  Closeable listener(final MarketDataStreamListener listener);

  Closeable tradeListener(final TradeDataStreamListener listener);

  interface Closeable {
    void close();
  }
}
