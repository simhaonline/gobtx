package com.gobtx.model.statistic;

import com.gobtx.model.enums.Exchange;

import java.math.BigDecimal;
import java.util.Objects;

/** Created by Aaron Kuai on 2019/12/23. This is on flush data so will not be persisted */
public class Trade24HStatistic {

  protected final Exchange exchange;
  protected final String symbol;

  private long timestamp; // This is the report time

  private BigDecimal open;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;

  private long count;
  private BigDecimal volume;
  private BigDecimal amount;

  private BigDecimal prevClosed;

  public Trade24HStatistic(Exchange exchange, String symbol) {
    this.exchange = exchange;
    this.symbol = symbol;
  }

  public Exchange getExchange() {
    return exchange;
  }

  public String getSymbol() {
    return symbol;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Trade24HStatistic setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public BigDecimal getOpen() {
    return open;
  }

  public Trade24HStatistic setOpen(BigDecimal open) {
    this.open = open;
    return this;
  }

  public BigDecimal getClose() {
    return close;
  }

  public Trade24HStatistic setClose(BigDecimal close) {
    this.close = close;
    return this;
  }

  public BigDecimal getHigh() {
    return high;
  }

  public Trade24HStatistic setHigh(BigDecimal high) {
    this.high = high;
    return this;
  }

  public BigDecimal getLow() {
    return low;
  }

  public Trade24HStatistic setLow(BigDecimal low) {
    this.low = low;
    return this;
  }

  public long getCount() {
    return count;
  }

  public Trade24HStatistic setCount(long count) {
    this.count = count;
    return this;
  }

  public BigDecimal getVolume() {
    return volume;
  }

  public Trade24HStatistic setVolume(BigDecimal volume) {
    this.volume = volume;
    return this;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Trade24HStatistic setAmount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  public BigDecimal getPrevClosed() {
    return prevClosed;
  }

  public Trade24HStatistic setPrevClosed(BigDecimal prevClosed) {
    this.prevClosed = prevClosed;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Trade24HStatistic that = (Trade24HStatistic) o;
    return exchange == that.exchange && Objects.equals(symbol, that.symbol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exchange, symbol);
  }
}
