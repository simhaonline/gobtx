package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

/** Created by Aaron Kuai on 2019/11/14. */
public class TableNameEachExchangeFactory implements TableNameFactory {

  public static final TableNameEachExchangeFactory INSTANCE = new TableNameEachExchangeFactory();

  @Override
  public String name(Exchange exchange, KlineInterval klineInterval) {
    return klineInterval.getTableSuffix();
  }
}
