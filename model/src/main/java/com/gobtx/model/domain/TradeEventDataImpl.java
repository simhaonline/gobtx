package com.gobtx.model.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** Created by Aaron Kuai on 2019/11/25. */
public class TradeEventDataImpl implements TradeEventData<TradeEventDataImpl> {

  protected String symbol;
  protected long reportTimestamp;
  protected long tradeTimestamp;
  protected BigDecimal price;
  protected BigDecimal quantity;
  protected boolean buyerMaker;
  protected boolean buy;

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public TradeEventDataImpl setSymbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  @Override
  public long getReportTimestamp() {
    return reportTimestamp;
  }

  @Override
  public TradeEventDataImpl setReportTimestamp(long reportTimestamp) {
    this.reportTimestamp = reportTimestamp;
    return this;
  }

  @Override
  public long getTradeTimestamp() {
    return tradeTimestamp;
  }

  @Override
  public TradeEventDataImpl setTradeTimestamp(long tradeTimestamp) {
    this.tradeTimestamp = tradeTimestamp;
    return this;
  }

  @Override
  public BigDecimal getPrice() {
    return price;
  }

  @Override
  public TradeEventDataImpl setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  @Override
  public BigDecimal getQuantity() {
    return quantity;
  }

  @Override
  public TradeEventDataImpl setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
    return this;
  }

  @Override
  public boolean isBuyerMaker() {
    return buyerMaker;
  }

  @Override
  public TradeEventDataImpl setBuyerMaker(boolean buyerMaker) {
    this.buyerMaker = buyerMaker;
    return this;
  }

  @Override
  public boolean isBuy() {
    return buy;
  }

  @Override
  public TradeEventDataImpl setBuy(boolean buy) {
    this.buy = buy;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TradeEventDataImpl that = (TradeEventDataImpl) o;
    return tradeTimestamp == that.tradeTimestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(tradeTimestamp);
  }

  @Override
  public String toString() {
    return "TradeEventDataImpl{"
        + "symbol='"
        + symbol
        + '\''
        + ", reportTimestamp="
        + reportTimestamp
        + ", tradeTimestamp="
        + tradeTimestamp
        + ", price="
        + price
        + ", quantity="
        + quantity
        + ", buyerMaker="
        + buyerMaker
        + ", buy="
        + buy
        + '}';
  }
}
