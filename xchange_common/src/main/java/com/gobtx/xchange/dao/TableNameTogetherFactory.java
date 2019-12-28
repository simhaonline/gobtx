package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

/** Created by Aaron Kuai on 2019/11/14. */
public class TableNameTogetherFactory implements TableNameFactory {

  public static final TableNameTogetherFactory INSTANCE = new TableNameTogetherFactory();

  @Override
  public String name(Exchange exchange, KlineInterval klineInterval) {
    return exchange.getName() + "_" + klineInterval.getTableSuffix();
  }
}
