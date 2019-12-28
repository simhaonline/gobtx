package com.gobtx.model.domain;

import java.io.Serializable;
import java.util.Objects;

/** Created by Aaron Kuai on 2019/11/14. */
public class OHLCKeyDataImpl
    implements OHLCKeyData<OHLCKeyDataImpl>, Serializable, Comparable<OHLCKeyDataImpl> {

  protected String symbol;
  protected long timeKey;

  public OHLCKeyDataImpl(String symbol, long timeKey) {
    this.symbol = symbol;
    this.timeKey = timeKey;
  }

  public OHLCKeyDataImpl() {}

  @Override
  public OHLCKeyData<OHLCKeyDataImpl> setSymbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  @Override
  public OHLCKeyData<OHLCKeyDataImpl> setTimeKey(long timeKey) {
    this.timeKey = timeKey;
    return this;
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public long getTimeKey() {
    return timeKey;
  }

  @Override
  public int compareTo(OHLCKeyDataImpl o) {
    return timeKey > o.timeKey ? -1 : 1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OHLCKeyDataImpl that = (OHLCKeyDataImpl) o;
    return timeKey == that.timeKey && Objects.equals(symbol, that.symbol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol, timeKey);
  }

  public static OHLCKeyData build(final String symbol, final long timeKey) {
    return new OHLCKeyDataImpl(symbol, timeKey);
  }

  @Override
  public String toString() {
    return "OHLCKey{" + "symbol='" + symbol + '\'' + ", timeKey=" + timeKey + '}';
  }
}
