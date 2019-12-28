package com.gobtx.xchange.configuration;

import com.gobtx.model.enums.Exchange;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Created by Aaron Kuai on 2019/12/23. */
public class LocalConfigurationProviderTest {

  private static final String BCH = "bchabcusdt";

  private static final String BCH_MAPPED = "BCHUSDT";

  private static String mapper(final String symbol) {

    switch (symbol) {
      case BCH:
        return BCH_MAPPED;
      default:
        return symbol.toUpperCase();
    }
  }

  @Test
  public void testPrintJs() {

    LocalConfigurationProvider provider = LocalConfigurationProvider.INSTANCE;

    final StringBuffer sb = new StringBuffer();

    int index = 0;

    final StringBuffer dataSB = new StringBuffer();

    dataSB.append("this._data = [\n");

    final Map<Exchange, StringBuffer> exchangeIndexMap = new LinkedHashMap<>();

    for (final Exchange exchange : Exchange.VALS) {

      // this._data = [
      //        {},
      //        {},
      //    ];
      //    this._dtIndex = {
      //        Binance: {"BTCUSDT": 1},
      //
      //    };

      Collection<String> symbols = provider.symbols(exchange);

      if (!symbols.isEmpty()) {

        final StringBuffer indexSB = new StringBuffer();
        exchangeIndexMap.put(exchange, indexSB);

        for (final String symbol : symbols) {

          dataSB.append("\t\t");
          dataSB.append(appendSymbol(exchange, symbol));
          dataSB.append(",\n");

          indexSB.append("\n\t\t\t");
          indexSB.append("'").append(mapper(symbol)).append("': ").append(index).append(",");

          index++;
        }
        // output.push({
        //                    'symbol': data[i],
        //                    'exchange': data[i + 1],
        //                    'price': price,
        //                    'change': (price * (changeRel / 100)).toFixed(2),
        //                    'changeRel': changeRel,
        //                    'volume': Math.floor(Math.random() * 100000)
        //                });

      }
    }
    dataSB.append("\n];\n");

    System.out.println(dataSB.toString());

    final StringBuffer ixSB = new StringBuffer();

    ixSB.append("\nthis._dtIndex = {\n\n");

    exchangeIndexMap.forEach(
        (k, v) -> {

          //        Binance: {"BTCUSDT": 1},

          ixSB.append("\n\t\t'").append(k).append("': {").append(v.toString()).append("},\n");
        });
    ixSB.append("\n};\n");

    System.out.println(ixSB.toString());
  }

  protected String appendSymbol(final Exchange exchange, final String symbol) {

    final StringBuffer sb = new StringBuffer();

    sb.append("{");

    sb.append("'")
        .append("symbol': '")
        .append(mapper(symbol))
        .append("', 'exchange': '")
        .append(exchange)
        .append("', 'price':'-', 'change':'-', 'changeRel':'-', 'volume': '-'");

    sb.append("}");

    return sb.toString();
  }
}
