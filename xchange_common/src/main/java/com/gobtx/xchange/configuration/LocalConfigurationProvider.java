package com.gobtx.xchange.configuration;

import com.gobtx.model.enums.Exchange;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Created by Aaron Kuai on 2019/11/25. */
public class LocalConfigurationProvider implements ConfigurationProvider {

  public static final LocalConfigurationProvider INSTANCE = new LocalConfigurationProvider();

  final List<String> BINANCE =
      Arrays.asList(
          "btcusdt", "ethusdt", "xrpusdt", "bchabcusdt", "ltcusdt", "eosusdt"
          // "bnbusdt",
          // - "bsvusdt",
          // "xlmusdt",
          // "trxusdt",
          // "adausdt",
          // "leousdt",
          // "xmrusdt",
          // "linkusdt",
          // "xtzusdt",
          // "neousdt"
          );

  // TODO: 2019/12/14  remove this test things
  final List<String> HUOBI =
      Arrays.asList(
          "btcusdt", "ethusdt", "xrpusdt", "bchusdt", "ltcusdt", "eosusdt"
          // - "bnbusdt",
          // "bsvusdt",
          // "xlmusdt",
          // "trxusdt",
          // "adausdt",
          // - "leousdt",
          // "xmrusdt",
          // "linkusdt",
          // "xtzusdt",
          // "neousdt",
          // "htusdt"
          );

  private LocalConfigurationProvider() {}

  @Override
  public Collection<String> symbols(Exchange exchange) {

    switch (exchange) {
      case BINANCE:
        return BINANCE;
      case HUOBI:
        return HUOBI;
    }
    return Collections.emptyList();
  }
}
