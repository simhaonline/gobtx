package com.gobtx.xchange.huobi;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.TradeEventDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.TradeEventView;
import com.gobtx.xchange.MarketDataStreamListener;
import com.gobtx.xchange.configuration.ConfigurationProvider;
import com.gobtx.xchange.configuration.LocalConfigurationProvider;
import com.gobtx.xchange.configuration.SymbolMapper;
import com.gobtx.xchange.exception.IllegalOperationException;
import com.gobtx.xchange.service.AbstractMarketDataService;
import com.gobtx.xchange.service.Version;
import com.huobi.client.SubscriptionClient;
import com.huobi.client.SyncRequestClient;
import com.huobi.client.model.Candlestick;
import com.huobi.client.model.Trade;
import com.huobi.client.model.enums.CandlestickInterval;
import com.huobi.client.model.enums.TradeDirection;
import com.huobi.client.model.request.CandlestickRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static com.gobtx.xchange.huobi.Constants.CURRENT;
import static com.gobtx.xchange.logger.DataLogger.appendData;
import static com.gobtx.xchange.logger.DataLogger.appendTrade;

/** Created by Aaron Kuai on 2019/11/13. */
@Component
public class HuobiMarketDataService extends AbstractMarketDataService {

  protected final HuobiInitiator initiator;

  protected ConfigurationProvider configurationProvider;

  public HuobiMarketDataService(
      @Autowired HuobiInitiator initiator,
      @Autowired(required = false) ConfigurationProvider configurationProvider) {
    this.initiator = initiator;
    this.configurationProvider = configurationProvider;
  }

  @Override
  protected Collection<String> initSymbols() {
    return configurationProvider.symbols(Exchange.HUOBI);
  }

  @Override
  protected void doStart() {
    // do the init to preload all the market data and then load the gap etc
    // And then do the subscribe by self

    if (configurationProvider == null) {
      configurationProvider = LocalConfigurationProvider.INSTANCE;
    }

    symbolMapper =
        new SymbolMapper() {
          @Override
          public String map(String symbol) {
            return symbol.toUpperCase();
          }
        };

    logger.warn("START_XCHANGE_CLIENT {}", exchange);

    // do all the things others
    try {
      final Collection<String> init = configurationProvider.symbols(Exchange.HUOBI);
      if (init.isEmpty()) {
        logger.warn("EXCHANGE_NO_INIT_SYMBOL {}", Exchange.HUOBI);
        return;
      } else {
        logger.warn("\n\nEXCHANGE_WITH_SYMBOLS HUOBI \n{}\n{}\n\n", init, supportKlineIntervals());
      }

      final SubscriptionClient subscriptionClient = SubscriptionClient.create();

      initiator.sync(
          init,
          symbolMapper,
          this,
          subscriptionClient,
          (statistic, success) -> {
            subscriptionClient.unsubscribeAll();

            logger.warn("DONE_SYNC HUOBI STATISTIC :{}\n{}\n", success, statistic);

            final String symbols = String.join(",", init).toLowerCase();

            doSubscribe(
                new KlineInterval[] {KlineInterval.m1, KlineInterval.h1, KlineInterval.d1},
                symbols,
                subscriptionClient);

            // do the trade information
            doSubscribeTradeEvent(symbols, subscriptionClient);

            logger.warn("FINISH_START_XCHANGE_CLIENT {}", exchange);

            afterStart();
          });

    } catch (Throwable throwable) {
      logger.error("FAIL_INIT_CLIENT {},{}", exchange, throwable);
    }
  }

  @Override
  protected int initDelayInSeconds() {
    return 15;
  }

  @Override
  protected int reconFrequentInSeconds() {
    return 3;
  }

  @Override
  public Closeable subscribeTradeEvent(String... symbols) {

    final String joinedSymbols = String.join(",", symbols).toLowerCase();
    return doSubscribeTradeEvent(joinedSymbols, SubscriptionClient.create());
  }

  private Closeable doSubscribeTradeEvent(
      final String joinedSymbols, SubscriptionClient subscriptionClient) {

    // final SubscriptionClient subscriptionClient = SubscriptionClient.create();

    logger.warn("START_SUB_TRADE_EVENT HUOBI {}", joinedSymbols);

    subscriptionClient.subscribeTradeEvent(
        joinedSymbols,
        data -> {
          if (!tradeListeners.isEmpty()
              && !(data.getTradeList() == null || data.getTradeList().isEmpty())) {

            final List<TradeEventView> dataList = new ArrayList<>(data.getTradeList().size());

            final String symbol = symbolMapper.map(data.getSymbol());

            for (final Trade trade : data.getTradeList()) {

              TradeEventDataImpl event = new TradeEventDataImpl();

              event
                  .setSymbol(symbol)
                  .setPrice(trade.getPrice())
                  .setQuantity(trade.getAmount())
                  .setReportTimestamp(data.getTimestamp())
                  .setTradeTimestamp(trade.getTimestamp())
                  .setBuy(trade.getDirection() == TradeDirection.BUY);

              dataList.add(event);

              appendTrade(event, Exchange.HUOBI);
            }

            tradeListeners.forEach(
                listener -> {
                  listener.updates(dataList, Exchange.HUOBI, symbol);
                });

          } else {
            logger.warn("NO_LISTENER_DROP_TRADE_EVENT HUOBI");
          }
        },
        exception -> {
          logger.error("HUOBI_FAIL_TRADE_EVENT {}", exception);
          //              try{
          //              subscriptionClient.unsubscribeAll();
          //              }catch (Throwable throwable){
          //                GlobalScheduleService.INSTANCE
          //                        .schedule(() -> {
          //                          doSubscribeTradeEvent(joinedSymbols);
          //                        }, 2, TimeUnit.MINUTES);
          //              }
        });

    return () -> subscriptionClient.unsubscribeAll();
  }

  @Override
  protected void doStop() {}

  private Closeable doSubscribe(
      final KlineInterval[] intervals,
      final String joinedSymbols,
      SubscriptionClient subscriptionClient) {

    // final SubscriptionClient subscriptionClient = SubscriptionClient.create();

    for (KlineInterval interval : intervals) {

      final CandlestickInterval mappedInterval = mapInternal(interval);

      logger.warn("TRY_SUBSCRIBE {},{},{}", exchange(), interval, joinedSymbols);

      subscriptionClient.subscribeCandlestickEvent(
          joinedSymbols,
          mappedInterval,
          data -> {
            // normalize the symbol to upper case
            final String symbol = symbolMapper.map(data.getSymbol());

            if (!listeners.isEmpty()) {

              final Candlestick input = data.getData();

              final OHLCDataImpl converted =
                  new OHLCDataImpl()
                      .setSymbol(symbol)
                      .setCloseTime(data.getTimestamp())
                      .setOpenTime(
                          input.getId() * 1_000) // second  --> ms while closeTime is missed in fact
                      .setAmount(input.getAmount())
                      .setVolume(input.getVolume())
                      .setNumberOfTrades(input.getCount())
                      .setOpen(input.getOpen())
                      .setClose(input.getClose())
                      .setLow(input.getLow())
                      .setHigh(input.getHigh());

              appendData(converted, Exchange.HUOBI, interval);

              listeners.forEach(it -> it.update(converted, interval, exchange, symbol));
            } else {

              logger.warn("NO_CONSUME_OF_HUOBI_MKT_DATA_STREAM {},{}", joinedSymbols, interval);
            }
          },
          exception -> logger.error("FAIL_HANDLE_HUOBI_STREAM {}", exception));
    }

    return () -> subscriptionClient.unsubscribeAll();
  }

  @Override
  public Closeable subscribe(final KlineInterval[] intervals, final String... symbols) {
    return doSubscribe(
        intervals, String.join(",", symbols).toLowerCase(), SubscriptionClient.create());
  }

  public Stream<OHLCData> history(
      final KlineInterval interval, final String symbol, final int limit, final long cutTimestamp) {

    final CandlestickInterval mappedInterval = mapInternal(interval);

    final SyncRequestClient syncRequestClient = SyncRequestClient.create();

    final int normalLimit = limit <= 0 ? 500 : (limit > 2000 ? 2000 : limit);

    List<Candlestick> candlesticks;

    if (cutTimestamp <= 0) {
      // this is just the latest one
      candlesticks = syncRequestClient.getLatestCandlestick(symbol, mappedInterval, normalLimit);

    } else {

      // this will go back to the history things

      CandlestickRequest request =
          new CandlestickRequest(symbol, mappedInterval, null, cutTimestamp, normalLimit);

      candlesticks = syncRequestClient.getCandlestick(request);
    }

    if (candlesticks == null || candlesticks.isEmpty()) {
      return Stream.empty();
    } else {

      return candlesticks.parallelStream().map(it -> mapOHLC(it, symbolMapper.map(symbol)));
    }
  }

  // -------Those are generic data

  protected Exchange exchange = Exchange.HUOBI;

  public Exchange exchange() {
    return Exchange.HUOBI;
  }

  public Version version() {
    return CURRENT;
  }

  public Set<KlineInterval> supportKlineIntervals() {
    return supportTypes;
  }

  @Override
  public Set<KlineInterval> supportReconKlineIntervals() {
    return supportTypes2;
  }

  protected static OHLCData mapOHLC(final Candlestick input, final String internalSymbol) {

    // [
    //  {
    //    "id": 1499184000,
    //    "amount": 37593.0266,
    //    "count": 0,
    //    "open": 1935.2000,
    //    "close": 1879.0000,
    //    "low": 1856.0000,
    //    "high": 1940.0000,
    //    "vol": 71031537.97866500
    //  }
    // ]

    return new OHLCDataImpl()
        .setSymbol(internalSymbol)
        .setOpenTime(input.getId() * 1_000) // second  --> ms while closeTime is missed in fact
        .setAmount(input.getAmount())
        .setVolume(input.getVolume())
        .setNumberOfTrades(input.getCount())
        .setOpen(input.getOpen())
        .setClose(input.getClose())
        .setLow(input.getLow())
        .setHigh(input.getHigh());
  }

  final Set<KlineInterval> supportTypes =
      new LinkedHashSet<KlineInterval>() {
        {
          add(KlineInterval.m1);
          add(KlineInterval.h1);
          add(KlineInterval.d1);
        }
      };

  final Set<KlineInterval> supportTypes2 =
      new LinkedHashSet<KlineInterval>() {
        {
          add(KlineInterval.m1);
          add(KlineInterval.m5);
          add(KlineInterval.m15);
          add(KlineInterval.m30);
          add(KlineInterval.h1);
          add(KlineInterval.h4);
          add(KlineInterval.d1);
          add(KlineInterval.w1);
          add(KlineInterval.M1);
          add(KlineInterval.Y1);
        }
      };

  protected static CandlestickInterval mapInternal(final KlineInterval interval) {

    switch (interval) {
      case m1:
        return CandlestickInterval.MIN1;
      case m5:
        return CandlestickInterval.MIN5;
      case m15:
        return CandlestickInterval.MIN15;
      case m30:
        return CandlestickInterval.MIN30;
      case h1:
        return CandlestickInterval.MIN60;
      case h4:
        return CandlestickInterval.HOUR4;
      case d1:
        return CandlestickInterval.DAY1;
      case w1:
        return CandlestickInterval.WEEK1;
      case M1:
        return CandlestickInterval.MON1;
      case Y1:
        return CandlestickInterval.YEAR1;
      case m3:
      case d3:
      case h2:
      case h6:
      case h12:
      default:
        throw new IllegalOperationException(
            "Huobi does not support kline history type: " + interval);
    }
  }

  protected List<MarketDataStreamListener> listeners() {
    return listeners;
  }
}
