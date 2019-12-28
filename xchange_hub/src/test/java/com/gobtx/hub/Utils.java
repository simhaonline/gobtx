package com.gobtx.hub;

import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.enums.KlineInterval;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Created by Aaron Kuai on 2019/11/18. */
public class Utils {

  public static List<OHLCDataImpl> loadTestData() throws IOException {

    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
    InputStream stream = Utils.class.getClassLoader().getResourceAsStream("sample/binance_m1.log");

    Reader reader = new InputStreamReader(stream, decoder);

    List<OHLCDataImpl> datas = new ArrayList<>();

    try (BufferedReader breader = new BufferedReader(reader)) {

      for (; ; ) {
        String line = breader.readLine();
        if (line == null) break;

        final String[] row = line.split("[|]");

        final String symbol = row[1];
        final KlineInterval klineInterval = KlineInterval.valueOf(row[2]);
        final long openTime = Long.parseLong(row[3]);
        final long closeTime = Long.parseLong(row[4]);

        final BigDecimal open = new BigDecimal(row[5]);
        final BigDecimal high = new BigDecimal(row[6]);
        final BigDecimal low = new BigDecimal(row[7]);
        final BigDecimal close = new BigDecimal(row[8]);
        final BigDecimal volume = new BigDecimal(row[10]);
        final BigDecimal amount = new BigDecimal(row[11]);
        final long numberOfTrades = Long.parseLong(row[12]);

        OHLCDataImpl data = new OHLCDataImpl();
        data.setSymbol(symbol)
            .setOpenTime(openTime)
            .setOpen(open)
            .setHigh(high)
            .setLow(low)
            .setClose(close)
            .setCloseTime(closeTime)
            .setVolume(volume)
            .setAmount(amount)
            .setNumberOfTrades(numberOfTrades);

        datas.add(data);
      }
    }

    return datas;
  }
}
