package com.gobtx.xchange.smock;

import com.gobtx.common.Env;
import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.service.AbstractMarketDataService;
import com.gobtx.xchange.service.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Created by Aaron Kuai on 2019/11/14. */
@Component
@SuppressWarnings("Duplicate")
public class MockMarketDataService extends AbstractMarketDataService {
  ScheduledFuture scheduledFuture;

  static Random random = new Random(System.currentTimeMillis());

  @Autowired ResourceLoader resourceLoader;

  @Override
  protected Collection<String> initSymbols() {
    return Arrays.asList("BTCUSDT");
  }

  private List<OHLCDataImpl> loadTestData() throws IOException {

    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
    InputStream stream = resourceLoader.getResource("sample/binance_m1.log").getInputStream();

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

  private static int getRandomNumberInRange(int min, int max) {
    return random.nextInt((max - min) + 1) + min;
  }

  private static double nextBTCPrice() {

    int big = getRandomNumberInRange(8000, 9000);
    int dot = getRandomNumberInRange(0, 100);

    return (double) big + (double) dot / 100d;
  }

  private static double nextETHPrice() {

    int big = getRandomNumberInRange(175, 190);
    int dot = getRandomNumberInRange(0, 100);

    return (double) big + (double) dot / 100d;
  }

  protected OHLCDataImpl mockBTCUSDT() {
    double n1 = nextBTCPrice();
    double n2 = nextBTCPrice();
    double n3 = nextBTCPrice();
    double n4 = nextBTCPrice();

    double high = n1, low = n1;

    if (n2 > n1) high = n2;
    if (n2 < low) low = n2;

    if (n3 > n1) high = n3;
    if (n3 < low) low = n3;

    if (n4 > n1) high = n4;
    if (n4 < low) low = n4;

    long openTime = System.currentTimeMillis() - 2_000;
    long closeTime = System.currentTimeMillis();

    OHLCDataImpl data = new OHLCDataImpl();
    data.setSymbol("BTCUSDT")
        .setOpenTime(openTime)
        .setOpen(BigDecimal.valueOf(n1))
        .setHigh(BigDecimal.valueOf(high))
        .setLow(BigDecimal.valueOf(low))
        .setClose(BigDecimal.valueOf(n4))
        .setCloseTime(closeTime)
        .setVolume(BigDecimal.valueOf(getRandomNumberInRange(100, 300)))
        .setAmount(BigDecimal.valueOf(getRandomNumberInRange(10000, 30000)))
        .setNumberOfTrades(getRandomNumberInRange(27, 58));

    return data;
  }

  protected OHLCDataImpl mockETHUSDT() {
    // 185

    double n1 = nextETHPrice();
    double n2 = nextETHPrice();
    double n3 = nextETHPrice();
    double n4 = nextETHPrice();

    double high = n1, low = n1;

    if (n2 > n1) high = n2;
    if (n2 < low) low = n2;

    if (n3 > n1) high = n3;
    if (n3 < low) low = n3;

    if (n4 > n1) high = n4;
    if (n4 < low) low = n4;

    long openTime = System.currentTimeMillis() - 2_000;
    long closeTime = System.currentTimeMillis();

    OHLCDataImpl data = new OHLCDataImpl();
    data.setSymbol("ETHUSDT")
        .setOpenTime(openTime)
        .setOpen(BigDecimal.valueOf(n1))
        .setHigh(BigDecimal.valueOf(high))
        .setLow(BigDecimal.valueOf(low))
        .setClose(BigDecimal.valueOf(n4))
        .setCloseTime(closeTime)
        .setVolume(BigDecimal.valueOf(getRandomNumberInRange(10000, 30000)))
        .setAmount(BigDecimal.valueOf(getRandomNumberInRange(10000, 30000)))
        .setNumberOfTrades(getRandomNumberInRange(270, 580));

    return data;
  }

  @Override
  protected void doStart() {

    if (Env.isProd()) {

      logger.error("PRODUCTION_SUPPOSE_NO_MOCK_SERVICE");
      return;
    }

    logger.debug("START_LOCAL_MOCK_MARKET_DATA_GENERATE");

    try {
      List<OHLCDataImpl> res = loadTestData();

      for (OHLCDataImpl data : res) {
        listeners.forEach(
            it -> {
              it.update(data, KlineInterval.m1, Exchange.MOCK, data.getSymbol());
            });
      }
    } catch (Throwable throwable) {
      logger.warn("FAIL_LOAD_MOCK {}", throwable);
    }

    scheduledFuture =
        GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
            () -> {

              // OHLCDataImpl data = mockBTCUSDT();

              OHLCDataImpl data2 = mockETHUSDT();

              listeners.forEach(
                  it -> {
                    it.update(data2, KlineInterval.m1, Exchange.MOCK, data2.getSymbol());
                  });
            },
            1,
            2,
            TimeUnit.SECONDS);
  }

  @Override
  protected void doStop() {
    scheduledFuture.cancel(true);
  }

  @Override
  public Closeable subscribe(KlineInterval[] intervals, String... symbols) {
    return () -> {};
  }

  @Override
  public Exchange exchange() {
    return Exchange.MOCK;
  }

  @Override
  public Version version() {
    return new Version(0, 0, 1);
  }

  @Override
  protected void afterStart() {}
}
