package com.gobtx.xchange.reference;

import com.gobtx.model.enums.Exchange;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/15. */
public class SymbolInfo {

  protected Exchange exchange;
  protected String symbol;

  // Whether the symbol base currency etc is force capital
  // This like the binance some time prefer capital in api arguments
  protected boolean forceCapital;

  protected String baseCurrency;
  protected String quoteCurrency;

  private int pricePrecision;
  private int amountPrecision;

  // This only meaning in huobi, like it is main or innovation?
  // private String symbolPartition;
  // private String symbol;

  private Integer valuePrecision;
  private BigDecimal minOrderAmt;
  private BigDecimal maxOrderAmt;
  private BigDecimal minOrderValue;

  private Integer leverageRatio;

  protected String extraInfo; // This is JSON information

  public SymbolInfo() {}
}
