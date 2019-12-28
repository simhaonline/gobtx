package com.gobtx.model.view;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

/** Created by Aaron Kuai on 2019/11/14. */
public interface OHLCWithExchangeAndIntervalView extends OHLCView {

  Exchange getExchange();

  KlineInterval getInterval();
}
