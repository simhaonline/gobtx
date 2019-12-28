package com.gobtx.frontend.ws.netty;

import com.gobtx.common.web.model.request.RequestPacket;
import com.gobtx.frontend.ws.config.ParamKey;
import com.gobtx.frontend.ws.netty.session.ChannelContextMap;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;

import static com.gobtx.frontend.ws.config.ParamKey.TRADE;
import static com.gobtx.frontend.ws.config.ParamKey.UNSUB_TRADE;
import static com.gobtx.frontend.ws.config.RequestPath.*;
import static com.gobtx.frontend.ws.json.JSonHelper.failRequest;
import static com.gobtx.frontend.ws.netty.Constants.CTX_MAP_ATTR_KEY;

/** Created by Aaron Kuai on 2019/11/19. */
@SuppressWarnings("Duplicates")
public class AcceptorWebSocketApplication extends WebSocketApplication {

  static final Logger logger = LoggerFactory.getLogger(AcceptorWebSocketApplication.class);

  private final Gson gson;

  static final WebSocketSubscribeContext WEB_SUB_CTX = WebSocketSubscribeContext.getInstance();

  public AcceptorWebSocketApplication(Gson gson) {
    this.gson = gson;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {

    final ChannelContextMap newHolder = new ChannelContextMap();

    final ChannelContextMap oldHolder = ctx.channel().attr(CTX_MAP_ATTR_KEY).getAndSet(newHolder);

    ctx.channel()
        .closeFuture()
        .addListener(
            future -> {
              // Purge all the context of the holder value fo the subscribe
              // TODO: 2019/11/20 a
              if (logger.isDebugEnabled()) {
                logger.debug("CLIENT_QUIT {}", ctx.channel().id());
              }
              WEB_SUB_CTX.offLine(ctx);
            });

    if (oldHolder != null) {

      // How to do this?
    }
  }

  @Override
  protected void handleReadIdle(ChannelHandlerContext ctx) {
    //
    logger.warn("CLIENT_READ_IDLE {}", ctx.channel());
  }

  @Override
  protected void handleWriteIdle(ChannelHandlerContext ctx) {
    logger.warn("CLIENT_WRITE_IDLE {}", ctx.channel());
  }

  /**
   * @param ctx
   * @param msg
   * @throws Exception
   * @see com.gobtx.common.web.model.request.RequestPacket
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

    if (msg instanceof TextWebSocketFrame) {

      // Parse this to a msg.content();

      final RequestPacket requestPacket =
          gson.fromJson(
              new InputStreamReader(new ByteBufInputStream(msg.content())), RequestPacket.class);

      if (logger.isDebugEnabled()) {
        logger.debug("WS_REQUEST  {}", requestPacket);
      }

      switch (requestPacket.getPath()) {
        case WORKSHOP:
          enterWorkshop(ctx, requestPacket);
          break;
        case SUBSCRIBE:
          subscribe(ctx, requestPacket);
          break;
        case UNSUBSCRIBE:
          unsubscribe(ctx, requestPacket);
          break;
        case KLINE:
          kline(ctx, requestPacket);
          break;
        case LOGIN:
          login(ctx, requestPacket);
          break;

        case LOGOUT:
          logout(ctx, requestPacket);
          break;

        case TRADE:
          subTradeEvent(ctx, requestPacket);
          break;

        case UNSUB_TRADE:
          unsubTradeEvent(ctx, requestPacket);
          break;

        case HEARTBEAT:
          break;

        default:
          logger.warn("UN_KNOWN_REQUEST {}", requestPacket);
          break;
      }

    } else if (msg instanceof BinaryWebSocketFrame) {

      logger.warn("BINARY_NOT_SUPPORT {}", ctx.channel());
      ctx.close();
    }
  }

  protected static boolean notEmpty(final Map target) {
    return target != null && !target.isEmpty();
  }

  protected static boolean empty(final String target) {
    return target == null || target.length() == 0;
  }

  public static final Pattern SPLIT_PATTERN = Pattern.compile(",", Pattern.LITERAL);

  private void subTradeEvent(final ChannelHandlerContext ctx, final RequestPacket request) {

    // Subscribe the trade and
    final Map parameters = request.getParameters();
    if (notEmpty(parameters)) {

      final String symbolsRaw = (String) parameters.get(ParamKey.SYMBOLS);
      final String exchangeRaw = (String) parameters.get(ParamKey.EXCHANGE);

      if (empty(symbolsRaw) || empty(exchangeRaw)) {
        // fail validate
        failProcess(ctx, request, 10001);
        return;
      } else {

        final Exchange exchange = Exchange.valueOf(exchangeRaw);
        if (logger.isDebugEnabled()) {
          logger.debug("TRY_SUB_TRADE_EVENT {},{}", exchange, symbolsRaw);
        }

        for (final String symbol : SPLIT_PATTERN.split(symbolsRaw)) {
          WEB_SUB_CTX.registerTicker(exchange, symbol, ctx);
        }
      }
    } else {
      failProcess(ctx, request, 20001);
    }
  }

  private void unsubTradeEvent(final ChannelHandlerContext ctx, final RequestPacket request) {
    final Map parameters = request.getParameters();
    if (notEmpty(parameters)) {

      final String symbolsRaw = (String) parameters.get(ParamKey.SYMBOLS);
      final String exchangeRaw = (String) parameters.get(ParamKey.EXCHANGE);

      if (empty(symbolsRaw) || empty(exchangeRaw)) {
        // fail validate
        failProcess(ctx, request, 10001);
        return;
      } else {
        final Exchange exchange = Exchange.valueOf(exchangeRaw);
        for (final String symbol : SPLIT_PATTERN.split(symbolsRaw)) {
          WEB_SUB_CTX.unregisterTicker(exchange, symbol, ctx);
        }
      }
    } else {
      failProcess(ctx, request, 20001);
    }
  }

  private void enterWorkshop(final ChannelHandlerContext ctx, final RequestPacket request) {

    // if user's entry the workshop
    // 1.  fetch all the data
    // 2.  fetch all latest kline  1 min
    // 3.  subscribe the tick data and Kline etc

    final Map parameters = request.getParameters();
    if (notEmpty(parameters)) {
      final String symbol = (String) parameters.get(ParamKey.SYMBOL);
      final String exchangeRaw = (String) parameters.get(ParamKey.EXCHANGE);

      if (empty(symbol) || empty(exchangeRaw)) {
        // fail validate
        failProcess(ctx, request, 10001);
        return;
      } else {

        final Exchange exchange = Exchange.valueOf(exchangeRaw);
        WEB_SUB_CTX.registerKline(exchange, KlineInterval.m1, symbol, ctx);
        WEB_SUB_CTX.registerTicker(exchange, symbol, ctx);
      }
    } else {
      failProcess(ctx, request, 20001);
    }
  }

  protected void subscribe(final ChannelHandlerContext ctx, RequestPacket request) {

    final Map parameters = request.getParameters();
    if (notEmpty(parameters)) {

      final String symbolsRaw = (String) parameters.get(ParamKey.SYMBOLS);
      final String exchangeRaw = (String) parameters.get(ParamKey.EXCHANGE);
      final String intervalRaw = (String) parameters.get(ParamKey.INTERVAL);

      if (empty(symbolsRaw) || empty(exchangeRaw) || empty(intervalRaw)) {
        // fail validate
        logger.warn("SUBSCRIBE_NOT_VALIDATE_PARAMS {}", request);
        failProcess(ctx, request, 10002);
        return;
      } else {

        // TODO: 2019/11/20  catch exceptions
        final Exchange exchange = Exchange.valueOf(exchangeRaw);
        final KlineInterval interval = KlineInterval.valueOf(intervalRaw);

        // try to subscribe things
        final LinkedHashSet<String> symbols = new LinkedHashSet<>();
        for (final String symbol : SPLIT_PATTERN.split(symbolsRaw)) {
          // TODO: 2019/11/20  exclude the illegal symbols
          symbols.add(symbol);
        }

        WEB_SUB_CTX.registerKlines(exchange, interval, symbols, ctx);
      }
    } else {
      failProcess(ctx, request, 20001);
    }
  }

  protected void unsubscribe(final ChannelHandlerContext ctx, RequestPacket request) {

    final Map parameters = request.getParameters();

    if (notEmpty(parameters)) {

      final String symbolsRaw = (String) parameters.get(ParamKey.SYMBOLS);
      final String exchangeRaw = (String) parameters.get(ParamKey.EXCHANGE);
      final String intervalRaw = (String) parameters.get(ParamKey.INTERVAL);

      if (empty(exchangeRaw)) {
        // fail validate
        failProcess(ctx, request, 10003);

      } else {

        final Exchange exchange = Exchange.valueOf(exchangeRaw);
        if (empty(symbolsRaw)) {
          // this is to clean up all the listener of this exchange
          WEB_SUB_CTX.unregisterKlineByExchange(exchange, ctx);
        } else {

          final LinkedHashSet<String> sys = new LinkedHashSet<>();
          for (final String symbol : SPLIT_PATTERN.split(symbolsRaw)) {
            sys.add(symbol);
          }
          if (empty(intervalRaw)) {
            // This is to clean up all the interval under this symbol
            WEB_SUB_CTX.unregisterKlineByExchangeAndSymbols(exchange, sys, ctx);
          } else {
            final KlineInterval interval = KlineInterval.valueOf(intervalRaw);
            WEB_SUB_CTX.unregisterKlines(exchange, interval, sys, ctx);
          }
        }
      }
    } else {
      failProcess(ctx, request, 20001);
    }
  }

  protected void kline(final ChannelHandlerContext ctx, RequestPacket request) {
    // This is to fetch the kline data

  }

  protected void login(final ChannelHandlerContext ctx, RequestPacket request) {
    // This is to mark the user login the ctx set up and update the user context session etc

  }

  protected void logout(final ChannelHandlerContext ctx, RequestPacket request) {
    // This is manually trigger so it will do the clean up job
    // Let the lifecycle to do the clean up jobs
    ctx.writeAndFlush(new TextWebSocketFrame()).addListener(ChannelFutureListener.CLOSE);
  }

  // ----------------------

  private void failProcess(
      final ChannelHandlerContext ctx, RequestPacket request, final int errorCode) {
    failProcess(ctx, request, errorCode, null);
  }

  private void failProcess(
      final ChannelHandlerContext ctx,
      RequestPacket request,
      final int errorCode,
      final String errorMessage) {

    ByteBuf error = failRequest(request, errorCode, errorMessage, ctx.alloc().buffer());
    ctx.writeAndFlush(new TextWebSocketFrame(error), ctx.voidPromise());
  }
}
