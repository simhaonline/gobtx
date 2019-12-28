package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;

import javax.sql.DataSource;

/** Created by Aaron Kuai on 2019/11/14. */
public class DataSourceExchangeAware {

  protected final DataSource dataSource;
  protected final Exchange exchange;
  protected final boolean mysql;

  public DataSourceExchangeAware(
      final DataSource dataSource, final Exchange exchange, final boolean mysql) {
    this.dataSource = dataSource;
    this.exchange = exchange;
    this.mysql = mysql;
  }

  public boolean isMysql() {
    return mysql;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public Exchange getExchange() {
    return exchange;
  }
}
