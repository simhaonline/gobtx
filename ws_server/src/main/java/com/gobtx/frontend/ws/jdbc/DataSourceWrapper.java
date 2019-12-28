package com.gobtx.frontend.ws.jdbc;

import com.gobtx.model.enums.Exchange;

import javax.sql.DataSource;

/** Created by Aaron Kuai on 2019/12/9. */
public class DataSourceWrapper {
  protected final DataSource dataSource;
  protected final Exchange exchange;
  protected final DBType dbType;

  public DataSourceWrapper(DataSource dataSource, Exchange exchange, DBType dbType) {
    this.dataSource = dataSource;
    this.exchange = exchange;
    this.dbType = dbType;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public Exchange getExchange() {
    return exchange;
  }

  public DBType getDbType() {
    return dbType;
  }
}
