package com.gobtx.xchange.dao;

/** Created by Aaron Kuai on 2019/11/14. */
public class MarketTableDDLTemplate {

  static final String ddl =
      "\n\ncreate table <table> ("
          + "\nsymbol varchar(12) not null,"
          + "\ntimeKey bigint default 0,"
          + "\nopenTime bigint default  0,"
          + "\ncloseTime bigint default 0,"
          + "\nopen DECIMAL(36,12) default 0, "
          + "\nhigh DECIMAL(36,12) default 0,"
          + "\nlow DECIMAL(36,12) default 0, "
          + "\nclose DECIMAL(36,12) default 0, "
          + "\nvolume DECIMAL(36,12) default 0, "
          + "\namount DECIMAL(36,12) default 0, "
          + "\nnumberOfTrades   bigint default  0,"
          + "\nprimary key (symbol, timeKey)"
          + "\n);"
          + "\n\n"
          + "\ncreate index <table>_symbol_index on <table> (symbol);"
          + "\ncreate index <table>_timeKey_index on <table> (timeKey);";

  public static String table(final String table) {
    return ddl.replace("<table>", table);
  }
}
