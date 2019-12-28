package com.gobtx.frontend.ws.service;

import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Created by Aaron Kuai on 2019/11/8. */
@Service
@Profile("mock-mkt-service")
@SuppressWarnings("Duplicates")
public class MarketDataServiceMock implements MarketDataService, InitializingBean {

  final ResourceLoader resourceLoader;

  public MarketDataServiceMock(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  private List<OHLCDataImpl> loadTestData() throws IOException {

    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
    InputStream stream =
        resourceLoader.getResource("classpath:sample/binance_m1.log").getInputStream();

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

  List<OHLCDataImpl> mocks;

  int size;

  @Override
  public void afterPropertiesSet() throws Exception {

    mocks = loadTestData();
    size = mocks.size();
  }

  //  List<OHLCView> mock =
  //      Arrays.asList(
  //          new OHLCDataImpl()
  //              .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(5))
  //              .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(4))
  //              .setOpen(BigDecimal.valueOf(123.11))
  //              .setHigh(BigDecimal.valueOf(125.88))
  //              .setLow(BigDecimal.valueOf(120.34))
  //              .setVolume(BigDecimal.valueOf(2323233))
  //              .setClose(BigDecimal.valueOf(124.12)),
  //          new OHLCDataImpl()
  //              .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(4))
  //              .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(3))
  //              .setOpen(BigDecimal.valueOf(113.11))
  //              .setHigh(BigDecimal.valueOf(115.88))
  //              .setLow(BigDecimal.valueOf(110.34))
  //              .setVolume(BigDecimal.valueOf(3323233))
  //              .setClose(BigDecimal.valueOf(114.12)),
  //          new OHLCDataImpl()
  //              .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(3))
  //              .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(2))
  //              .setOpen(BigDecimal.valueOf(133.11))
  //              .setHigh(BigDecimal.valueOf(135.88))
  //              .setLow(BigDecimal.valueOf(130.34))
  //              .setVolume(BigDecimal.valueOf(2823233))
  //              .setClose(BigDecimal.valueOf(134.12)));

  @Override
  public List<OHLCView> data(
      final String symbol,
      final KlineInterval interval,
      final Exchange exchange,
      final long startTime,
      final long enTime,
      boolean first) {

    final List<OHLCView> res = new ArrayList<>();
    int loop = size;
    for (final OHLCDataImpl each : mocks) {
      res.add(each.setOpenTime(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(loop--)));
    }
    return res;
  }

  @Override
  public int order() {
    return Integer.MAX_VALUE;
  }
}
