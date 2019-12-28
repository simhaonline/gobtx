package com.gobtx.frontend.ws.push;

import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import org.junit.Test;

import java.math.BigDecimal;

import static java.util.concurrent.TimeUnit.MINUTES;

/** Created by Aaron Kuai on 2019/12/7. */
public class LocalMarketDataLastSnapshotterTest {
  @Test
  public void handle() {

    LocalMarketDataLastSnapshotter snapshotter = LocalMarketDataLastSnapshotter.INSTANCE;

    // OHLCWithExchangeAndInternalData

    OHLCDataImpl data =
        new OHLCDataImpl()
            .setSymbol("USDJPY")
            .setTimeKey(4)
            .setOpenTime(System.currentTimeMillis() - MINUTES.toMillis(3))
            .setCloseTime(System.currentTimeMillis() - MINUTES.toMillis(2))
            .setOpen(BigDecimal.valueOf(3333))
            .setHigh(BigDecimal.valueOf(3333))
            .setLow(BigDecimal.valueOf(3333))
            .setVolume(BigDecimal.valueOf(3333))
            .setClose(BigDecimal.valueOf(3333));

    final OHLCWithExchangeAndInternalData ohlc =
        new OHLCWithExchangeAndInternalData(Exchange.BINANCE, KlineInterval.d1, data);

    snapshotter.handle(ohlc);

    System.out.println(snapshotter.lastSnapshot());
  }
}
