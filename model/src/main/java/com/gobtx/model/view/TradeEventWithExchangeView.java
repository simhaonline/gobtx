package com.gobtx.model.view;

import com.gobtx.model.enums.Exchange;

/** Created by Aaron Kuai on 2019/11/25. */
public interface TradeEventWithExchangeView extends TradeEventView {
  Exchange getExchange();
}
