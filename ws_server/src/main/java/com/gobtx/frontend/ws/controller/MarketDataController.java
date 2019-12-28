package com.gobtx.frontend.ws.controller;

import com.gobtx.common.ErrorCode;
import com.gobtx.common.web.Result;
import com.gobtx.common.web.ResultGenerator;
import com.gobtx.frontend.ws.local.CompositeMarketDataService;
import com.gobtx.frontend.ws.model.request.MarketKlineRequest;
import com.gobtx.frontend.ws.news.NewsSpider;
import com.gobtx.frontend.ws.service.MarketDataService;
import com.gobtx.frontend.ws.utils.FrontErrorCode;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.statistic.Trade24HManager;
import com.gobtx.model.view.OHLCView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Aaron Kuai on 2019/11/8.
 *
 * <p>This is used to load the market history data the Kline data
 */
@RestController
@RequestMapping("/market")
public class MarketDataController {

  static final Logger logger = LoggerFactory.getLogger(MarketDataController.class);

  protected MarketDataService marketDataService;

  protected final NewsSpider newsSpider;

  //  protected MarketDataLastSnapshotter snapshotter;

  protected final Trade24HManager manager;

  public MarketDataController(
      @Autowired List<MarketDataService> marketDataServices,
      // @Autowired(required = false) final MarketDataLastSnapshotter snapshotter,
      @Autowired NewsSpider newsSpider,
      @Autowired(required = false) Trade24HManager manager) {

    this.newsSpider = newsSpider;
    this.manager = manager;

    if (marketDataServices.size() == 1) {
      marketDataService = marketDataServices.get(0);
    } else {

      marketDataServices.sort(Comparator.comparingInt(MarketDataService::order));

      logger.warn(
          "COMPOSITE_MARKET_SERVICE  {},{}",
          marketDataServices.get(0).getClass(),
          marketDataServices.get(1).getClass());
      marketDataService =
          new CompositeMarketDataService(marketDataServices.get(0), marketDataServices.get(1));
    }

    logger.warn("MARKET_SERVICE_START {}", marketDataService.getClass());
    marketDataService.start();

    // this.snapshotter = snapshotter == null ? LocalMarketDataLastSnapshotter.INSTANCE :
    // snapshotter;
  }

  @RequestMapping(value = "/news")
  public Result news(@RequestParam(required = false) final Long timestamp) {
    return ResultGenerator.success(newsSpider.latest(10));
  }

  @RequestMapping(value = "/list")
  public Result list(@RequestParam(required = false) final String exchange) {
    // List the index of the exchange

    return ResultGenerator.success(manager.statistics());

    //    List<OHLCWithExchangeAndIntervalView> res;
    //
    //    if (exchange != null && !exchange.isEmpty()) {
    //      Exchange ex = null;
    //      try {
    //        ex = Exchange.valueOf(exchange.toUpperCase());
    //      } catch (Throwable throwable) {
    //        logger.warn("Illegal input exchange  {},{}", exchange, throwable);
    //      }
    //      if (ex != null) {
    //        res = snapshotter.lastSnapshot(KlineInterval.m1);
    //      } else {
    //        res = snapshotter.lastSnapshot(ex, KlineInterval.m1);
    //      }
    //
    //    } else {
    //      res = snapshotter.lastSnapshot(KlineInterval.m1);
    //    }
    //
    //    if (res != null && !res.isEmpty()) {
    //      return ResultGenerator.success(res);
    //    }
    //
    //    return ResultGenerator.success();
  }

  /**
   * UDF model
   *
   * @param request
   * @param result
   */
  @RequestMapping(value = "/history")
  public Result kline(@Valid MarketKlineRequest request, final BindingResult result) {

    //    if (Env.isDev() && !request.isFirst()) {
    //      return ResultGenerator.success();
    //    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "HISTORY_GET {},{},{},{},{}",
          request.isFirst(),
          request.getStartTime(),
          request.getEndTime(),
          request.getSymbol(),
          request.getExchange());
    }

    //    getContext()
    //        .map(SecurityContext::getAuthentication)
    //        .map(
    //            (Function<Authentication, Void>)
    //                authentication -> {
    //                  if (authentication == null) {
    //                    logger.debug("NOTHING DOING");
    //                    return null;
    //                  }
    //                  if (authentication.isAuthenticated()) {
    //
    //                    logger.debug("WHO_ACCESS {}", authentication.getPrincipal());
    //                  } else {
    //                    logger.debug("ANO_ACCESS!!!!!!!!!!!");
    //                  }
    //
    //                  return null;
    //                });

    // MediaType.APPLICATION_JSON
    // Should we track the user's action

    // http://localhost:8088/market/history?symbol=EURJPY&interval=m1&startTime=20000000&limit=200&exchange=binance
    // HV000030: No validator could be found for constraint 'javax.validation.constraints.NotEmpty'
    // validating type 'java.lang.String'. Check configuration for 'symbol'

    if (result.hasErrors()) {

      logger.warn("FAIL_VALIDATE_MKT_REQUEST {}", result.getFieldError().getDefaultMessage());

      return ResultGenerator.fail(
          ErrorCode.safeValueOf(FrontErrorCode.class, result.getFieldError().getDefaultMessage()));
    }

    KlineInterval interval;

    try {
      interval = KlineInterval.valueOf(request.getInterval());
    } catch (Throwable throwable) {
      logger.error("FAIL_CONVERTER_KLINE {}", throwable);
      interval = KlineInterval.m1;
      // return ResultGenerator.fail(FrontErrorCode.KLINE_INTERVAL_ILLEGAL);
    }

    Exchange exchange;
    try {
      exchange = Exchange.valueOf(request.getExchange().toUpperCase());
    } catch (Throwable throwable) {
      logger.error("FAIL_CONVERTER_EXCHANGE {}", throwable);
      return ResultGenerator.fail(FrontErrorCode.EXCHANGE_ILLEGAL);
    }

    // bars.push({time: item[0], open: item[1], high: item[2], low: item[3], close: item[4], volume:
    // item[5]})

    long start = System.currentTimeMillis();

    List<? extends OHLCView> ohlcs =
        marketDataService.data(
            request.getSymbol(),
            interval,
            exchange,
            request.getStartTime(),
            request.getEndTime() > 0
                ? (request.getEndTime() > request.getStartTime()
                    ? request.getEndTime()
                    : request.getStartTime() + 50_000)
                : System.currentTimeMillis(),
            request.isFirst());

    // This is really fuck me where the time is cost
    // GET_HISTORY_COST BINANCE,m1,37,1470
    logger.warn(
        "GET_HISTORY_COST {},{},[{},{}],{},{}",
        exchange,
        interval,
        new Date(request.getStartTime()),
        new Date(request.getEndTime()),
        (System.currentTimeMillis() - start),
        ohlcs.size());

    if (ohlcs != null && !ohlcs.isEmpty()) {

      return ResultGenerator.success(
          ohlcs.stream()
              .map(
                  it ->
                      Arrays.asList(
                          it.getOpenTime(),
                          it.getOpen(),
                          it.getHigh(),
                          it.getLow(),
                          it.getClose(),
                          it.getVolume()))
              .collect(Collectors.toList()));
    }

    return ResultGenerator.success();
  }
}
