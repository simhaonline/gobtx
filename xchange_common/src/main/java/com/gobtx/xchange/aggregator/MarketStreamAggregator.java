package com.gobtx.xchange.aggregator;

import com.gobtx.xchange.MarketDataStreamListener;

/** Created by Aaron Kuai on 2019/11/11. */
public interface MarketStreamAggregator extends MarketDataStreamListener {

  void start();

  void stop();

  Closeable postAggregateListener(final PostAggregateListener listener);

  interface Closeable {
    void close();
  }
}
