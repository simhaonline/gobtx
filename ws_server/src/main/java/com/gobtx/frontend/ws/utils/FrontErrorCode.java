package com.gobtx.frontend.ws.utils;

import com.gobtx.common.ErrorCode;

/** Created by Aaron Kuai on 2019/11/8. */
public enum FrontErrorCode implements ErrorCode {
  SYMBOL_IS_NULL(1, "Symbol suppose not be null or empty"),
  KLINE_INTERVAL_ILLEGAL(2, "K Line interval is illegal"),
  START_TIME_ILLEGAL(3, "Start time is illegal"),
  LIMIT_EXCEED(4, "Limit most is 500"),
  EXCHANGE_IS_NULL(5, "Exchange should not be null or empty"),
  EXCHANGE_ILLEGAL(6, "Exchange can not match");

  private static final int prefix = 1_000_000;

  private final int value;

  protected final int code;

  protected final String message;

  FrontErrorCode(int value, String message) {
    this.value = value;
    this.message = message;
    this.code = prefix + value;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String message() {
    return message;
  }

  @Override
  public int getPrefix() {
    return prefix;
  }
}
