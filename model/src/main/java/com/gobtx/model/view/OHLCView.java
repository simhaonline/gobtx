package com.gobtx.model.view;

import com.gobtx.model.domain.OHLCKeyDataImpl;

import java.math.BigDecimal;

/** The protocol interface to get the open high low close price of the candle stick price */
public interface OHLCView {

  default OHLCKeyView compositeKey() {
    return OHLCKeyDataImpl.build(getSymbol(), getTimeKey());
  }

  /**
   * The symbol also is the key
   *
   * @return
   */
  String getSymbol();

  /**
   * Optimized key used in our system
   *
   * @return
   */
  long getTimeKey();

  /**
   * Epoch time ms
   *
   * @return
   */
  long getOpenTime();

  /**
   * Epoch time ms
   *
   * @return
   */
  long getCloseTime();

  BigDecimal getOpen();

  BigDecimal getHigh();

  BigDecimal getLow();

  BigDecimal getClose();

  /**
   * This is base on the base currency like BTC/USDT this is the BTC
   *
   * @return
   */
  BigDecimal getVolume();

  /**
   * This is base on the Quote Currency like BTC/USDT this is the USDT if this is not over-write
   * this is default to the volume
   *
   * @return
   */
  default BigDecimal getAmount() {
    return getVolume();
  }

  /**
   * How many trades happened this is kind like the base currency volume This may lead to a lot of
   * confuse, will be totally depend on the implement of the underline exchange details
   *
   * @return
   */
  default long getNumberOfTrades() {
    return 0L;
  }
}
