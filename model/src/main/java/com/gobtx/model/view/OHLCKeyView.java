package com.gobtx.model.view;

/** Created by Aaron Kuai on 2019/11/14. */
public interface OHLCKeyView {

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
}
