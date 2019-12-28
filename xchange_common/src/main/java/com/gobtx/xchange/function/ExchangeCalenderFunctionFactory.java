package com.gobtx.xchange.function;

import com.gobtx.model.enums.Exchange;

import java.util.Calendar;

/** Created by Aaron Kuai on 2019/11/11. */
public class ExchangeCalenderFunctionFactory {

  static final ExchangeCalenderFunctionFactory INSTANCE = new ExchangeCalenderFunctionFactory();

  private static ExchangeCalenderFunction[] functions;

  private static final ExchangeCalenderFunction df;

  static {
    functions = new ExchangeCalenderFunction[Exchange.values().length];

    df =
        new ExchangeCalenderFunction() {

          @Override
          public long keyFromCalender(Calendar calendar, final String symbol) {
            return calendar.getTimeInMillis();
          }

          @Override
          public Calendar calenderFromKey(long key, final String symbol) {
            final Calendar res = Calendar.getInstance();
            res.setTimeInMillis(key);
            return res;
          }
        };

    for (int i = 0; i < functions.length; i++) {

      functions[i] = df;
    }
  }

  public static final ExchangeCalenderFunctionFactory getInstance() {
    return INSTANCE;
  }

  private ExchangeCalenderFunctionFactory() {}

  public void register(
      final Exchange exchange, final ExchangeCalenderFunction exchangeCalenderFunction) {
    functions[exchange.ordinal()] = exchangeCalenderFunction;
  }

  public ExchangeCalenderFunction function(final Exchange exchange) {
    return functions[exchange.ordinal()];
  }
}
