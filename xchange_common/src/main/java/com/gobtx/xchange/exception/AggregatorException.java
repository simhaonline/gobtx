package com.gobtx.xchange.exception;

/** Created by Aaron Kuai on 2019/11/11. */
public class AggregatorException extends Exception {

  private static final long serialVersionUID = 2493582234456248734L;

  public AggregatorException() {}

  public AggregatorException(String message) {
    super(message);
  }

  public AggregatorException(String message, Throwable cause) {
    super(message, cause);
  }

  public AggregatorException(Throwable cause) {
    super(cause);
  }

  public AggregatorException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
