package com.gobtx.model.domain;

import com.gobtx.model.view.OHLCView;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/** Created by Aaron Kuai on 2019/11/12. */
public class OHLCDataImpl
    implements OHLCData<OHLCDataImpl>, Serializable, Comparable<OHLCDataImpl> {

  protected String symbol;
  protected long timeKey;

  protected long openTime;
  protected long closeTime;
  protected BigDecimal open;
  protected BigDecimal high;
  protected BigDecimal low;
  protected BigDecimal close;

  // Previous is the normally the key thing
  protected BigDecimal volume;
  protected BigDecimal amount;
  protected long numberOfTrades;

  @Override
  public long getTimeKey() {
    return timeKey;
  }

  @Override
  public OHLCDataImpl setTimeKey(long timeKey) {
    this.timeKey = timeKey;
    return this;
  }

  @Override
  public long getOpenTime() {
    return openTime;
  }

  @Override
  public OHLCDataImpl setOpenTime(long openTime) {
    this.openTime = openTime;
    return this;
  }

  @Override
  public long getCloseTime() {
    return closeTime;
  }

  @Override
  public OHLCDataImpl setCloseTime(long closeTime) {
    this.closeTime = closeTime;
    return this;
  }

  @Override
  public BigDecimal getOpen() {
    return open;
  }

  @Override
  public OHLCDataImpl setOpen(BigDecimal open) {
    this.open = open;
    return this;
  }

  @Override
  public BigDecimal getHigh() {
    return high;
  }

  @Override
  public OHLCDataImpl setHigh(BigDecimal high) {
    this.high = high;
    return this;
  }

  @Override
  public BigDecimal getLow() {
    return low;
  }

  @Override
  public OHLCDataImpl setLow(BigDecimal low) {
    this.low = low;
    return this;
  }

  @Override
  public BigDecimal getClose() {
    return close;
  }

  @Override
  public OHLCDataImpl setClose(BigDecimal close) {
    this.close = close;
    return this;
  }

  @Override
  public BigDecimal getVolume() {
    return volume;
  }

  @Override
  public OHLCDataImpl setVolume(BigDecimal volume) {
    this.volume = volume;
    return this;
  }

  @Override
  public BigDecimal getAmount() {
    return amount;
  }

  @Override
  public OHLCDataImpl setAmount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  @Override
  public long getNumberOfTrades() {
    return numberOfTrades;
  }

  @Override
  public OHLCDataImpl setNumberOfTrades(long numberOfTrades) {
    this.numberOfTrades = numberOfTrades;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OHLCDataImpl ohlcData = (OHLCDataImpl) o;
    return timeKey == ohlcData.timeKey;
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeKey);
  }

  @Override
  public int compareTo(OHLCDataImpl other) {
    // DESC
    return this.getTimeKey() > other.getTimeKey() ? 1 : -1;
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public OHLCDataImpl setSymbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  public static OHLCData copy(final OHLCView view) {

    if (view instanceof OHLCData) return (OHLCData) view;

    return new OHLCDataImpl()
        .setSymbol(view.getSymbol())
        .setTimeKey(view.getTimeKey())
        .setOpenTime(view.getOpenTime())
        .setCloseTime(view.getCloseTime())
        .setOpen(view.getOpen())
        .setHigh(view.getHigh())
        .setLow(view.getLow())
        .setClose(view.getClose())
        .setVolume(view.getVolume())
        .setAmount(view.getAmount())
        .setNumberOfTrades(view.getNumberOfTrades());
  }

  @Override
  public String toString() {
    return "OHLCDataImpl{"
        + "symbol='"
        + symbol
        + '\''
        + ", timeKey="
        + timeKey
        + ", openTime="
        + openTime
        + ", closeTime="
        + closeTime
        + ", open="
        + open
        + ", high="
        + high
        + ", low="
        + low
        + ", close="
        + close
        + ", volume="
        + volume
        + ", amount="
        + amount
        + ", numberOfTrades="
        + numberOfTrades
        + '}';
  }
}
