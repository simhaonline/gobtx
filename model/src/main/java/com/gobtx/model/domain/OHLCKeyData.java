package com.gobtx.model.domain;

import com.gobtx.model.view.OHLCKeyView;

/** Created by Aaron Kuai on 2019/11/14. */
public interface OHLCKeyData<T extends OHLCKeyData> extends OHLCKeyView {
  OHLCKeyData<T> setSymbol(String symbol);

  OHLCKeyData<T> setTimeKey(long timeKey);
}
