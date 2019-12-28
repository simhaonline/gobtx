package com.gobtx.model.statistic;

/** Created by Aaron Kuai on 2019/12/23. */
@FunctionalInterface
public interface Trade24HStatistic3rdPartyCallBack {
  void success(final Trade24HStatistic statistic, boolean first);

  default void fail(final String symbol, final Throwable throwable) {};
}
