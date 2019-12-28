package com.gobtx.xchange.exception;

/** Created by Aaron Kuai on 2019/11/13. */
public class IllegalOperationException extends UnsupportedOperationException {

  private static final long serialVersionUID = -7191326402380187443L;

  public IllegalOperationException() {}

  public IllegalOperationException(String message) {
    super(message);
  }

  public IllegalOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalOperationException(Throwable cause) {
    super(cause);
  }
}
