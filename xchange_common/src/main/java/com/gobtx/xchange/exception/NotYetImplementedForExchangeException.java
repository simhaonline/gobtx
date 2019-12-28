package com.gobtx.xchange.exception;

/** Created by Aaron Kuai on 2019/11/13. */
public class NotYetImplementedForExchangeException extends UnsupportedOperationException {

  private static final long serialVersionUID = 8950561754252894694L;

  public NotYetImplementedForExchangeException() {}

  public NotYetImplementedForExchangeException(String message) {
    super(message);
  }

  public NotYetImplementedForExchangeException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotYetImplementedForExchangeException(Throwable cause) {
    super(cause);
  }
}
