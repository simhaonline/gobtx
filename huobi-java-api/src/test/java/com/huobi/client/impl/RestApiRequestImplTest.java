package com.huobi.client.impl;

import com.huobi.client.impl.utils.JsonWrapper;
import com.huobi.client.impl.utils.JsonWrapperArray;
import com.huobi.client.model.Symbol;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Created by Aaron Kuai on 2019/12/24. */
public class RestApiRequestImplTest {

  @Test
  public void getSymbols() throws IOException {

    InputStream stream =
        RestApiRequestImplTest.class.getClassLoader().getResourceAsStream("huobi.symbols.json");

    Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8.newDecoder());

    final StringBuffer sbs = new StringBuffer();
    try (BufferedReader breader = new BufferedReader(reader)) {

      for (; ; ) {
        String line = breader.readLine();
        if (line == null) break;

        sbs.append(line);
      }
    }

    JsonWrapper jsonWrapper = JsonWrapper.parseFromString(sbs.toString());

    Map<String, Symbol> symbolMap = new LinkedHashMap<>();
    JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
    dataArray.forEach(
        (item) -> {
          Symbol symbol = new Symbol();
          symbol.setBaseCurrency(item.getString("base-currency"));
          symbol.setQuoteCurrency(item.getString("quote-currency"));
          symbol.setPricePrecision(item.getInteger("price-precision"));
          symbol.setAmountPrecision(item.getInteger("amount-precision"));
          symbol.setSymbolPartition(item.getString("symbol-partition"));
          symbol.setSymbol(item.getString("symbol"));
          symbol.setValuePrecision(item.getIntegerOrDefault("value-precision", null));
          symbol.setMinOrderAmt(item.getBigDecimalOrDefault("min-order-amt", null));
          symbol.setMaxOrderAmt(item.getBigDecimalOrDefault("max-order-amt", null));
          symbol.setMinOrderValue(item.getBigDecimalOrDefault("min-order-value", null));
          symbol.setLeverageRatio(item.getIntegerOrDefault("leverage-ratio", null));

          symbolMap.put(symbol.getSymbol(), symbol);
        });

    // "btcusdt", "ethusdt", "xrpusdt", "bchusdt", "ltcusdt", "eosusdt"

    final List<String> HUOBI =
        Arrays.asList("btcusdt", "ethusdt", "xrpusdt", "bchusdt", "ltcusdt", "eosusdt");

    StringBuilder sb = new StringBuilder();

    // const SYMBOL_PRECISION = {
    //    'BTCUSDT': {
    //        pp: 2,//PricePrecision
    //        ap: 2 //AmountPrecision
    //    },
    //    'ETHUSDT': 1,
    //    'XRPUSDT': 2,
    //    'BCHUSDT': 3,
    //    'LTCUSDT': 4,
    //    'EOSUSDT': 5,
    //
    // }

    sb.append("const SYMBOL_PRECISION = {\n");
    for (final String symbol : HUOBI) {
      final Symbol sl = symbolMap.get(symbol);

      if (sl != null) {
        // 'BTCUSDT': {
        //        pp: 2,//PricePrecision
        //        ap: 2 //AmountPrecision
        //    },
        //    'ETHUSDT': 1,
        //    'XRPUSDT': 2,
        //    'BCHUSDT': 3,
        //    'LTCUSDT': 4,
        //    'EOSUSDT': 5,

        sb.append("\n\t\t\t'").append(symbol.toUpperCase()).append("': {\n");

        sb.append("\t\t\t\tpp: ").append(sl.getPricePrecision()).append(",\n");
        sb.append("\t\t\t\tap: ").append(sl.getAmountPrecision());
        sb.append("\n\t\t\t\t},");

        System.out.println(
            symbol.toUpperCase() + "  " + sl.getPricePrecision() + "  " + sl.getAmountPrecision());

      } else {
        System.err.println("Sth fucked me " + symbol);
      }
    }
    sb.append("\n}\n");

    System.out.println(sb.toString());
  }
}
