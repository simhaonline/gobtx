package com.gobtx.model.dto;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;

import java.math.BigDecimal;

/**
 * Created by Aaron Kuai on 2019/11/14.
 *
 * <p>This data is for the network process
 */
public class OHLCWithExchangeAndInternalData implements OHLCWithExchangeAndIntervalView {

  public static final OHLCWithExchangeAndInternalData DEAD =
      new OHLCWithExchangeAndInternalData(Exchange.MOCK, KlineInterval.m1, null);

  protected final Exchange exchange;
  protected final KlineInterval interval;

  protected final OHLCView data;

  public OHLCWithExchangeAndInternalData(
      final Exchange exchange, final KlineInterval interval, final OHLCView delegate) {
    this.exchange = exchange;
    this.interval = interval;
    this.data = delegate;
  }

  public int getType() {
    return 0;
  }

  @Override
  public Exchange getExchange() {
    return exchange;
  }

  @Override
  public KlineInterval getInterval() {
    return interval;
  }

  @Override
  public String getSymbol() {
    return data.getSymbol();
  }

  @Override
  public long getTimeKey() {
    return data.getTimeKey();
  }

  @Override
  public long getOpenTime() {
    return data.getOpenTime();
  }

  @Override
  public long getCloseTime() {
    return data.getCloseTime();
  }

  @Override
  public BigDecimal getOpen() {
    return data.getOpen();
  }

  @Override
  public BigDecimal getHigh() {
    return data.getHigh();
  }

  @Override
  public BigDecimal getLow() {
    return data.getLow();
  }

  @Override
  public BigDecimal getClose() {
    return data.getClose();
  }

  @Override
  public BigDecimal getVolume() {
    return data.getVolume();
  }

  @Override
  public BigDecimal getAmount() {
    return data.getAmount();
  }

  @Override
  public long getNumberOfTrades() {
    return data.getNumberOfTrades();
  }

  @Override
  public String toString() {
    return "OHLCWithExchangeAndInternalData{"
        + "exchange="
        + exchange
        + ", interval="
        + interval
        + ", data="
        + data
        + '}';
  }
}
