package com.gobtx.model.statistic;

import com.gobtx.model.enums.Exchange;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;

/** Created by Aaron Kuai on 2019/12/23. */
public interface Trade24HProvider {

  default Collection<Trade24HStatistic> statistic() {
    return Collections.emptyList();
  }

  Closeable listener(final Trade24HProviderListener listener);

  void start();

  void stop();

  Exchange exchange();
}
