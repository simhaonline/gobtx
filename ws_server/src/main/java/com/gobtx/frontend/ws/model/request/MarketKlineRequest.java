package com.gobtx.frontend.ws.model.request;

import com.gobtx.model.enums.KlineInterval;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

/** Created by Aaron Kuai on 2019/11/8. */
public class MarketKlineRequest implements Serializable {

  @Size(min = 3, message = "SYMBOL_IS_NULL")
  protected String symbol;

  @Size(min = 2, max = 2, message = "KLINE_INTERVAL_ILLEGAL")
  protected String interval = KlineInterval.m1.name();

  @Min(value = 1000000, message = "START_TIME_ILLEGAL")
  protected long startTime;

  protected long endTime = 0;

  @Max(value = 500, message = "LIMIT_EXCEED")
  protected int limit;

  @Size(min = 3, message = "EXCHANGE_IS_NULL")
  protected String exchange;

  protected boolean first = true;

  public boolean isFirst() {
    return first;
  }

  public MarketKlineRequest setFirst(boolean first) {
    this.first = first;
    return this;
  }

  public MarketKlineRequest() {}

  public String getSymbol() {
    return symbol;
  }

  public MarketKlineRequest setSymbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  public String getInterval() {
    return interval;
  }

  public MarketKlineRequest setInterval(String interval) {
    this.interval = interval;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public MarketKlineRequest setStartTime(long startTime) {
    this.startTime = startTime;
    return this;
  }

  public int getLimit() {
    return limit;
  }

  public MarketKlineRequest setLimit(int limit) {
    this.limit = limit;
    return this;
  }

  public String getExchange() {
    return exchange;
  }

  public MarketKlineRequest setExchange(String exchange) {
    this.exchange = exchange;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public MarketKlineRequest setEndTime(long endTime) {
    this.endTime = endTime;
    return this;
  }
}
