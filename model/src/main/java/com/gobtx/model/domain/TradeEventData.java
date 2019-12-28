package com.gobtx.model.domain;

import com.gobtx.model.view.TradeEventView;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/25. */
public interface TradeEventData<T extends TradeEventData> extends TradeEventView {

  TradeEventData<T> setSymbol(String symbol);

  TradeEventData<T> setReportTimestamp(long reportTimestamp);

  TradeEventData<T> setTradeTimestamp(long tradeTimestamp);

  TradeEventData<T> setPrice(BigDecimal price);

  TradeEventData<T> setQuantity(BigDecimal quantity);

  TradeEventData<T> setBuyerMaker(boolean buyerMaker);

  TradeEventData<T> setBuy(boolean buy);
}
