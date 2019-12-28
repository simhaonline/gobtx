package com.gobtx.frontend.ws.jdbc;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCKeyView;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/12/10. */
public class StubOHLCWithExchangeAndIntervalView implements OHLCWithExchangeAndIntervalView {

  protected final long openTime;

  public StubOHLCWithExchangeAndIntervalView(long openTime) {
    this.openTime = openTime;
  }

  @Override
  public long getOpenTime() {
    return openTime;
  }

  @Override
  public Exchange getExchange() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public KlineInterval getInterval() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public OHLCKeyView compositeKey() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public String getSymbol() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public long getTimeKey() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public long getCloseTime() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public BigDecimal getOpen() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public BigDecimal getHigh() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public BigDecimal getLow() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public BigDecimal getClose() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public BigDecimal getVolume() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public BigDecimal getAmount() {
    throw new IllegalStateException("Stub this method is wrong");
  }

  @Override
  public long getNumberOfTrades() {
    throw new IllegalStateException("Stub this method is wrong");
  }
}
