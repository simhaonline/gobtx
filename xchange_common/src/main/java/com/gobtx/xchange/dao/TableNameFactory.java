package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

/** Created by Aaron Kuai on 2019/11/14. */
public interface TableNameFactory {

  default String name(final Exchange exchange, final KlineInterval klineInterval) {
    return klineInterval.name();
  }
}
