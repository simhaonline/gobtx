package com.gobtx.xchange.binance;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.gobtx.common.Utils;
import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.TradeEventDataImpl;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.MarketDataStreamListener;
import com.gobtx.xchange.configuration.ConfigurationProvider;
import com.gobtx.xchange.configuration.LocalConfigurationProvider;
import com.gobtx.xchange.configuration.SymbolMapper;
import com.gobtx.xchange.exception.IllegalOperationException;
import com.gobtx.xchange.service.AbstractMarketDataService;
import com.gobtx.xchange.service.Version;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.gobtx.model.enums.KlineInterval.*;
import static com.gobtx.xchange.logger.DataLogger.appendData;
import static com.gobtx.xchange.logger.DataLogger.appendTrade;

@Component
@SuppressWarnings("Duplicates")
public class BinanceMarketDataService extends AbstractMarketDataService {

  protected final BinanceInitiator initiator;

  protected ConfigurationProvider configurationProvider;

  private static final String BCH = "bchabcusdt";

  private static final String BCH_MAPPED = "BCHUSDT";

  public static String map(final String symbol) {

    switch (symbol) {
      case BCH:
        return BCH_MAPPED;
      default:
        return symbol.toUpperCase();
    }
  }

  public BinanceMarketDataService(
      @Autowired BinanceInitiator initiator,
      @Autowired(required = false) ConfigurationProvider configurationProvider) {
    this.initiator = initiator;
    this.configurationProvider = configurationProvider;
  }

  @Override
  protected void doStart() {

    if (configurationProvider == null) {
      configurationProvider = LocalConfigurationProvider.INSTANCE;
    }

    symbolMapper =
        new SymbolMapper() {
          @Override
          public String map(final String symbol) {

            switch (symbol) {
              case BCH:
                return BCH_MAPPED;
              default:
                return symbol.toUpperCase();
            }
          }
        };

    logger.warn("START_XCHANGE_CLIENT {}", exchange());

    try {

      final Collection<String> init = configurationProvider.symbols(Exchange.BINANCE);
      if (init.isEmpty()) {
        logger.warn("EXCHANGE_NO_INIT_SYMBOL {}", Exchange.BINANCE);
        return;
      } else {
        logger.warn(
            "\n\nEXCHANGE_WITH_SYMBOLS BINANCE \n{}\n{}\n\n", init, supportKlineIntervals());
      }

      // This need to be transformed as it will load from the
      initiator.sync(
          init,
          symbolMapper,
          this,
          null,
          (statistic, success) -> {
            logger.warn("DONE_SYNC BINANCE STATISTIC :{}\n{}\n", success, statistic);

            final String symbols = String.join(",", init).toLowerCase();

            doSubscribe(new KlineInterval[] {m1, h1, d1}, symbols);
            doSubscribeTradeEvent(symbols);

            logger.warn("FINISH_START_XCHANGE_CLIENT {}", exchange());

            afterStart();
          });

    } catch (Throwable throwable) {
      logger.error("FAIL_INIT_CLIENT {},{}", exchange(), throwable);
    }
  }

  @Override
  protected Collection<String> initSymbols() {
    return configurationProvider.symbols(Exchange.BINANCE);
  }

  @Override
  public Closeable subscribeTradeEvent(String... symbols) {
    final String symbolsList = String.join(",", symbols).toLowerCase();
    return doSubscribeTradeEvent(symbolsList);
  }

  private void watchDog(final KlineInterval interval, final String symbols) {

    logger.warn("SUBSCRIBE_FAIL_NEED_RE_SUB {},{},{}", Exchange.BINANCE, interval, symbols);

    final Retryer<Boolean> retryer =
        RetryerBuilder.<Boolean>newBuilder()
            .retryIfResult(Predicates.<Boolean>isNull())
            .retryIfRuntimeException()
            .withStopStrategy(StopStrategies.neverStop())
            .withWaitStrategy(WaitStrategies.fixedWait(15, TimeUnit.SECONDS))
            .build();

    final BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    final BinanceApiWebSocketClient client = factory.newWebSocketClient();

    final long kickOffTime = System.currentTimeMillis();

    final Callable<Boolean> callable =
        () -> {
          final CandlestickInterval mapInterval = mapKline(interval);

          final java.io.Closeable[] closeable = new java.io.Closeable[1];

          closeable[0] =
              client.onCandlestickEvent(
                  symbols,
                  mapInterval,
                  new BinanceApiCallback<CandlestickEvent>() {

                    public void onResponse(CandlestickEvent response) {

                      if (!listeners.isEmpty()) {

                        final String symbol = symbolMapper.map(response.getSymbol());

                        OHLCDataImpl updated =
                            new OHLCDataImpl()
                                .setSymbol(symbol)
                                .setOpenTime(response.getOpenTime())
                                .setCloseTime(response.getCloseTime())
                                .setOpen(new BigDecimal(response.getOpen()))
                                .setHigh(new BigDecimal(response.getHigh()))
                                .setLow(new BigDecimal(response.getLow()))
                                .setClose(new BigDecimal(response.getClose()))
                                .setVolume(new BigDecimal(response.getVolume()))
                                .setAmount(new BigDecimal(response.getQuoteAssetVolume()))
                                .setNumberOfTrades(response.getNumberOfTrades());

                        appendData(updated, Exchange.BINANCE, interval);

                        listeners.forEach(
                            it -> {
                              it.update(updated, interval, Exchange.BINANCE, symbol);
                            });

                      } else {

                        logger.warn("NO_LISTENER_READY {},{}", exchange(), response);
                      }
                    }

                    @Override
                    public void onFailure(Throwable cause) {

                      Utils.closeQuietly(closeable[0]);
                      logger.warn(
                          "WATCH_DOG_INSTANCE_FAIL {},{},{}", Exchange.BINANCE, symbols, cause);

                      if ((System.currentTimeMillis() - kickOffTime)
                          < TimeUnit.MINUTES.toMillis(2)) {
                        // if two minute may block by the binance?

                        logger.warn(
                            "LESS_2_MINUTES_FAIL_WAIT_FOR_NEXT_2_MINUTES {},{}",
                            Exchange.BINANCE,
                            symbols);

                        GlobalScheduleService.INSTANCE.schedule(
                            () -> watchDog(interval, symbols), 2, TimeUnit.MINUTES);

                      } else {
                        GlobalScheduleService.INSTANCE.schedule(
                            () -> watchDog(interval, symbols), 10, TimeUnit.SECONDS);
                      }
                    }
                  });

          return true;
        };

    try {
      retryer.call(callable);
    } catch (Throwable e) {
      logger.warn("FAIL_KICK_RETRY_OF {},{},{}", Exchange.BINANCE, symbols, e);
    }
  }

  @Override
  protected void doStop() {}

  @Override
  public Stream<OHLCData> history(
      KlineInterval interval, String inputSymbol, int limit, long cutTimestamp) {

    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    BinanceApiRestClient client = factory.newRestClient();

    CandlestickInterval mappedInterval = mapKline(interval);
    final int normalLimit = limit <= 0 ? 500 : (limit > 1000 ? 1000 : limit);

    final String symbol = symbolMapper.map(inputSymbol);

    List<Candlestick> candlestickBars;
    if (cutTimestamp > 0) {
      candlestickBars =
          client.getCandlestickBars(
              symbol.toUpperCase(), mappedInterval, normalLimit, null, cutTimestamp);
    } else {
      candlestickBars = client.getCandlestickBars(symbol.toUpperCase(), mappedInterval);
    }

    if (candlestickBars == null || candlestickBars.isEmpty()) {
      return Stream.empty();
    } else {
      return candlestickBars.parallelStream().map(it -> map(it, symbol));
    }
  }

  private Closeable doSubscribeTradeEvent(final String symbolsList) {

    final BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    final BinanceApiWebSocketClient client = factory.newWebSocketClient();

    final java.io.Closeable[] closeable = new java.io.Closeable[1];

    logger.warn("START_SUB_TRADE_EVENT {},{}", Exchange.BINANCE, symbolsList);

    closeable[0] =
        client.onAggTradeEvent(
            symbolsList,
            new BinanceApiCallback<AggTradeEvent>() {
              @Override
              public void onResponse(AggTradeEvent response) {

                if (!tradeListeners.isEmpty()) {

                  TradeEventDataImpl event = new TradeEventDataImpl();

                  event
                      .setSymbol(symbolMapper.map(response.getSymbol()))
                      .setPrice(new BigDecimal(response.getPrice()))
                      .setQuantity(new BigDecimal(response.getQuantity()))
                      .setReportTimestamp(response.getEventTime())
                      .setTradeTimestamp(response.getTradeTime())
                      .setBuyerMaker(response.isBuyerMaker());

                  appendTrade(event, Exchange.BINANCE);

                  tradeListeners.forEach(
                      listener -> {
                        listener.update(event, Exchange.BINANCE, event.getSymbol());
                      });
                } else {
                  logger.warn("NO_LISTENER_DROP_TRADE_EVENT BINANCE");
                }
              }

              @Override
              public void onFailure(Throwable cause) {
                watchDogTradeEvent(symbolsList);
              }
            });

    return () -> Utils.closeQuietly(closeable[0]);
  }

  private void watchDogTradeEvent(final String symbols) {

    logger.warn("RE_SUBSCRIBE_TRADE_EVENT {},{}", Exchange.BINANCE, symbols);

    final Retryer<Boolean> retryer =
        RetryerBuilder.<Boolean>newBuilder()
            .retryIfResult(Predicates.<Boolean>isNull())
            .retryIfRuntimeException()
            .withStopStrategy(StopStrategies.neverStop())
            .withWaitStrategy(WaitStrategies.fixedWait(15, TimeUnit.SECONDS))
            .build();

    final BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();

    final BinanceApiWebSocketClient client = factory.newWebSocketClient();

    final Callable<Boolean> callable =
        () -> {
          final long kickOffTime = System.currentTimeMillis();

          final java.io.Closeable[] closeable = new java.io.Closeable[1];

          closeable[0] =
              client.onAggTradeEvent(
                  symbols,
                  new BinanceApiCallback<AggTradeEvent>() {
                    @Override
                    public void onResponse(AggTradeEvent response) {

                      if (!tradeListeners.isEmpty()) {

                        TradeEventDataImpl event = new TradeEventDataImpl();

                        event
                            .setSymbol(symbolMapper.map(response.getSymbol()))
                            .setPrice(new BigDecimal(response.getPrice()))
                            .setQuantity(new BigDecimal(response.getQuantity()))
                            .setReportTimestamp(response.getEventTime())
                            .setTradeTimestamp(response.getTradeTime())
                            .setBuyerMaker(response.isBuyerMaker());

                        appendTrade(event, Exchange.BINANCE);

                        tradeListeners.forEach(
                            listener -> {
                              listener.update(event, Exchange.BINANCE, event.getSymbol());
                            });
                      } else {
                        logger.warn("NO_LISTENER_DROP_TRADE_EVENT BINANCE");
                      }
                    }

                    @Override
                    public void onFailure(Throwable cause) {

                      Utils.closeQuietly(closeable[0]);
                      logger.warn(
                          "WATCH_DOG_INSTANCE_FAIL {},{},{}", Exchange.BINANCE, symbols, cause);

                      if ((System.currentTimeMillis() - kickOffTime)
                          < TimeUnit.MINUTES.toMillis(2)) {
                        // if two minute may block by the binance?

                        logger.warn(
                            "LESS_2_MINUTES_FAIL_WAIT_FOR_NEXT_2_MINUTES {},{}",
                            Exchange.BINANCE,
                            symbols);

                        GlobalScheduleService.INSTANCE.schedule(
                            () -> watchDogTradeEvent(symbols), 2, TimeUnit.MINUTES);

                      } else {
                        GlobalScheduleService.INSTANCE.schedule(
                            () -> watchDogTradeEvent(symbols), 15, TimeUnit.SECONDS);
                      }
                    }
                  });

          return true;
        };

    try {
      retryer.call(callable);
    } catch (Throwable e) {
      logger.warn("FAIL_KICK_RETRY_OF_TRADE {},{},{}", Exchange.BINANCE, symbols, e);
    }
  }

  private Closeable doSubscribe(final KlineInterval[] intervals, final String symbolsList) {

    final BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    final BinanceApiWebSocketClient client = factory.newWebSocketClient();

    final List<java.io.Closeable> closeableList = new ArrayList<>();

    for (KlineInterval interval : intervals) {

      final CandlestickInterval mapInterval = mapKline(interval);

      logger.warn("TRY_SUBSCRIBE {},{},{}", exchange(), interval, symbolsList);

      final java.io.Closeable[] closeable = new java.io.Closeable[1];

      closeable[0] =
          client.onCandlestickEvent(
              symbolsList,
              mapInterval,
              new BinanceApiCallback<CandlestickEvent>() {
                @Override
                public void onResponse(CandlestickEvent response) {

                  if (!listeners.isEmpty()) {

                    final String symbol = symbolMapper.map(response.getSymbol());

                    OHLCDataImpl updated =
                        new OHLCDataImpl()
                            .setSymbol(symbol)
                            .setOpenTime(response.getOpenTime())
                            .setCloseTime(response.getCloseTime())
                            .setOpen(new BigDecimal(response.getOpen()))
                            .setHigh(new BigDecimal(response.getHigh()))
                            .setLow(new BigDecimal(response.getLow()))
                            .setClose(new BigDecimal(response.getClose()))
                            .setVolume(new BigDecimal(response.getVolume()))
                            .setAmount(new BigDecimal(response.getQuoteAssetVolume()))
                            .setNumberOfTrades(response.getNumberOfTrades());

                    appendData(updated, Exchange.BINANCE, interval);

                    listeners.forEach(
                        it -> {
                          it.update(updated, interval, Exchange.BINANCE, symbol);
                        });

                  } else {

                    logger.warn("NO_LISTENER_READY {},{}", exchange(), response);
                  }
                }

                @Override
                public void onFailure(Throwable cause) {

                  // This is hook to the OkHttpClient#WebSocketListener

                  //
                  // Invoked when a web socket has been closed due to an error reading from or
                  // writing to the
                  // network. Both outgoing and incoming messages may have been lost. No further
                  // calls to this
                  // listener will be made.
                  //
                  // onFailure(WebSocket webSocket, Throwable t, @Nullable Response response)
                  // @Override
                  //  public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                  //    if (!closing) {
                  //      callback.onFailure(t);
                  //    }
                  //
                  //  }
                  // This make sense the websocket is closed by mistake

                  Utils.closeQuietly(closeable[0]);
                  logger.error("UN_CATCH_FAILURE {},{}", exchange(), cause);
                  // This may cause multiple trigger
                  // Try to reconnect to the binance

                  watchDog(interval, symbolsList);
                }
              });

      closeableList.add(closeable[0]);
    }

    return () ->
        closeableList.forEach(
            it -> {
              try {
                it.close();
              } catch (Throwable e) {
                logger.warn("FAIL_CLOSE_BINANCE_STREAM_CLIENT {}", e);
              }
            });
  }

  @Override
  public Closeable subscribe(final KlineInterval[] intervals, final String... symbols) {

    return doSubscribe(intervals, String.join(",", symbols).toLowerCase());
  }

  protected static OHLCData map(final Candlestick candlestick, final String symbol) {

    return new OHLCDataImpl()
        .setSymbol(symbol)
        .setOpenTime(candlestick.getOpenTime())
        .setCloseTime(candlestick.getCloseTime())
        .setOpen(new BigDecimal(candlestick.getOpen()))
        .setHigh(new BigDecimal(candlestick.getHigh()))
        .setLow(new BigDecimal(candlestick.getLow()))
        .setClose(new BigDecimal(candlestick.getClose()))
        .setVolume(new BigDecimal(candlestick.getVolume()))
        .setAmount(new BigDecimal(candlestick.getQuoteAssetVolume()))
        .setNumberOfTrades(candlestick.getNumberOfTrades());
  }

  static final Set<KlineInterval> support =
      new LinkedHashSet<KlineInterval>() {

        {
          add(m1);
          add(m3);
          add(m5);
          add(m15);
          add(m30);
          add(h1);
          add(h2);
          add(h4);
          add(h6);
          add(h8);
          add(h12);
          add(d1);
          add(d3);
          add(w1);
          add(M1);
        }
      };

  @Override
  public Set<KlineInterval> supportKlineIntervals() {
    return support;
  }

  @Override
  public Exchange exchange() {
    return Exchange.BINANCE;
  }

  @Override
  public Version version() {
    return Constants.CURRENT;
  }

  public static CandlestickInterval mapKline(final KlineInterval interval) {

    switch (interval) {
      case m1:
        return CandlestickInterval.ONE_MINUTE;
      case m3:
        return CandlestickInterval.THREE_MINUTES;
      case m5:
        return CandlestickInterval.FIVE_MINUTES;
      case m15:
        return CandlestickInterval.FIFTEEN_MINUTES;
      case m30:
        return CandlestickInterval.HALF_HOURLY;
      case h1:
        return CandlestickInterval.HOURLY;
      case h2:
        return CandlestickInterval.TWO_HOURLY;
      case h4:
        return CandlestickInterval.FOUR_HOURLY;
      case h6:
        return CandlestickInterval.SIX_HOURLY;
      case h8:
        return CandlestickInterval.EIGHT_HOURLY;
      case h12:
        return CandlestickInterval.TWELVE_HOURLY;
      case d1:
        return CandlestickInterval.DAILY;
      case d3:
        return CandlestickInterval.THREE_DAILY;
      case w1:
        return CandlestickInterval.WEEKLY;
      case M1:
        return CandlestickInterval.MONTHLY;
      case Y1:
      default:
        throw new IllegalOperationException(
            "Binance does not support kline history type: " + interval);
    }
  }

  protected List<MarketDataStreamListener> listeners() {
    return listeners;
  }
}
