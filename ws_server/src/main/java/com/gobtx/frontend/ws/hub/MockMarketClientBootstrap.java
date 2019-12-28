package com.gobtx.frontend.ws.hub;

import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/** Created by Aaron Kuai on 2019/11/26. */
@Profile("mock-hub")
@Component
public class MockMarketClientBootstrap implements HubBootstrap {

  static final Logger logger = LoggerFactory.getLogger(MockMarketClientBootstrap.class);

  @Autowired(required = false)
  MarketHubClientListener listener;

  @Autowired ResourceLoader resourceLoader;

  private List<OHLCDataImpl> data;

  @Override
  public void start(EventLoopGroup workerEventloop) {

    if (listener == null) {
      logger.warn("NO_DOWN_STREAM_LISTENER_MOCK_NOTHING");
      return;
    }

    try {
      data = loadTestData();
    } catch (IOException e) {
      logger.error("FAIL_LOAD_MOCK_TEST_DATA {}", e);
    }

    final int size = data.size();

    GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {

            final OHLCDataImpl data =
                MockMarketClientBootstrap.this.data.get(getRandomNumberInRange(0, size - 1));

            data.setOpenTime(System.currentTimeMillis())
                .setCloseTime(System.currentTimeMillis())
                .setTimeKey(System.currentTimeMillis());

            OHLCWithExchangeAndInternalData event =
                new OHLCWithExchangeAndInternalData(Exchange.BINANCE, KlineInterval.m1, data);

            listener.handle(event);
            // logger.debug("PUBLISH_MOCK_MKT_DATA {}", data);
          }
        },
        1,
        1,
        TimeUnit.SECONDS);
  }

  static Random random = new Random(System.currentTimeMillis());

  private static int getRandomNumberInRange(int min, int max) {
    return random.nextInt((max - min) + 1) + min;
  }

  public void stop() {}

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
}
