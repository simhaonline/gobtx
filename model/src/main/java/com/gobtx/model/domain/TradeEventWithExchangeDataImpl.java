package com.gobtx.model.domain;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.view.TradeEventWithExchangeView;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/25. */
public class TradeEventWithExchangeDataImpl
    implements TradeEventWithExchangeView, TradeEventData<TradeEventWithExchangeDataImpl> {

  protected Exchange exchange;
  protected String symbol;
  protected long reportTimestamp;
  protected long tradeTimestamp;
  protected BigDecimal price;
  protected BigDecimal quantity;
  protected boolean buyerMaker;
  protected boolean buy;

  public int getType() {
    return 1;
  }

  public Exchange getExchange() {
    return exchange;
  }

  public TradeEventWithExchangeDataImpl setExchange(Exchange exchange) {
    this.exchange = exchange;
    return this;
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public TradeEventWithExchangeDataImpl setSymbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  @Override
  public long getReportTimestamp() {
    return reportTimestamp;
  }

  @Override
  public TradeEventWithExchangeDataImpl setReportTimestamp(long reportTimestamp) {
    this.reportTimestamp = reportTimestamp;
    return this;
  }

  @Override
  public long getTradeTimestamp() {
    return tradeTimestamp;
  }

  @Override
  public TradeEventWithExchangeDataImpl setTradeTimestamp(long tradeTimestamp) {
    this.tradeTimestamp = tradeTimestamp;
    return this;
  }

  @Override
  public BigDecimal getPrice() {
    return price;
  }

  @Override
  public TradeEventWithExchangeDataImpl setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  @Override
  public BigDecimal getQuantity() {
    return quantity;
  }

  @Override
  public TradeEventWithExchangeDataImpl setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
    return this;
  }

  @Override
  public boolean isBuyerMaker() {
    return buyerMaker;
  }

  @Override
  public TradeEventWithExchangeDataImpl setBuyerMaker(boolean buyerMaker) {
    this.buyerMaker = buyerMaker;
    return this;
  }

  @Override
  public boolean isBuy() {
    return buy;
  }

  @Override
  public TradeEventWithExchangeDataImpl setBuy(boolean buy) {
    this.buy = buy;
    return this;
  }
}
