package com.gobtx.model.view;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/25. */
public interface TradeEventView {

  /**
   * The symbol also is the key
   *
   * @return
   */
  String getSymbol();

  long getReportTimestamp();

  long getTradeTimestamp();

  BigDecimal getPrice();

  BigDecimal getQuantity();

  // ---Execution only
  // This is extra things

  boolean isBuyerMaker();

  // This is for the Huobi
  boolean isBuy();
}
