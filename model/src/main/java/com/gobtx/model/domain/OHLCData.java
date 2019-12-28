package com.gobtx.model.domain;

import com.gobtx.model.view.OHLCView;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/12. */
public interface OHLCData<T extends OHLCData> extends OHLCView {

  OHLCData<T> setSymbol(String symbol);

  OHLCData<T> setTimeKey(long timeKey);

  OHLCData<T> setOpenTime(long openTime);

  OHLCData<T> setCloseTime(long closeTime);

  OHLCData<T> setOpen(BigDecimal open);

  OHLCData<T> setHigh(BigDecimal high);

  OHLCData<T> setLow(BigDecimal low);

  OHLCData<T> setClose(BigDecimal close);

  OHLCData<T> setVolume(BigDecimal volume);

  OHLCData<T> setAmount(BigDecimal amount);

  OHLCData<T> setNumberOfTrades(long numberOfTrades);
}
