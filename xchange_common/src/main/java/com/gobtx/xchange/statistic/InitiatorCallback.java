package com.gobtx.xchange.statistic;

/** Created by Aaron Kuai on 2019/12/12. */
public interface InitiatorCallback {
  void done(final BatchLoadStatistic statistic, final boolean success);
}
