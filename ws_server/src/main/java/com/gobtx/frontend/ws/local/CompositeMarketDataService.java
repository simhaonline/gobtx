package com.gobtx.frontend.ws.local;

import com.gobtx.frontend.ws.service.MarketDataService;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;

import java.util.List;

/**
 * Created by Aaron Kuai on 2019/12/6.
 *
 * <p>1. backend with the JDBC and Ignite and etc
 */
public class CompositeMarketDataService implements MarketDataService {

  // Local tree map things
  // 1min is 1440

  //
  private final MarketDataService primaryDataService;
  private final MarketDataService backendDataService;

  public CompositeMarketDataService(
      final MarketDataService primaryDataService, final MarketDataService backendDataService) {

    this.primaryDataService = primaryDataService;
    this.backendDataService = backendDataService;
  }

  @Override
  public void start() {

    primaryDataService.start();
    backendDataService.start();
  }

  @Override
  public void stop() {
    primaryDataService.stop();
    backendDataService.stop();
  }

  @Override
  public List<? extends OHLCView> data(
      final String symbol,
      final KlineInterval interval,
      final Exchange exchange,
      final long startTime,
      final long endTime,
      final boolean first) {

    try {
      return primaryDataService.data(symbol, interval, exchange, startTime, endTime, first);
    } catch (Throwable throwable) {
      return backendDataService.data(symbol, interval, exchange, startTime, endTime, first);
    }
  }
}
