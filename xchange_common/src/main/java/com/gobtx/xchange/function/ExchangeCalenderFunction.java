package com.gobtx.xchange.function;

import java.util.Calendar;

/** Created by Aaron Kuai on 2019/11/11. */
public interface ExchangeCalenderFunction {

  default long keyFromCalender(final Calendar calendar, final String symbol) {
    return calendar.getTimeInMillis();
  }

  Calendar calenderFromKey(final long key, final String symbol);
}
