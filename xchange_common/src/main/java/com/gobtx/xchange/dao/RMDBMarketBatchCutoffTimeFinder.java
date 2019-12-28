package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.gobtx.common.Utils.closeQuietly;

/** Created by Aaron Kuai on 2019/12/13. */
public class RMDBMarketBatchCutoffTimeFinder implements MarketBatchCutoffTimeFinder {

  static final Logger logger = LoggerFactory.getLogger(RMDBMarketBatchCutoffTimeFinder.class);

  public static final String SELECT_TEMPLATE = "SELECT max(openTime) FROM <Table> WHERE symbol = ?";

  public static final RMDBMarketBatchCutoffTimeFinder INSTANCE =
      new RMDBMarketBatchCutoffTimeFinder();

  protected final RMDBContext[][] context = new RMDBContext[Exchange.VALS.length][];

  private RMDBMarketBatchCutoffTimeFinder() {

    for (int i = 0; i < Exchange.VALS.length; i++) {
      context[i] = new RMDBContext[KlineInterval.VALS.length];
      for (int j = 0; j < KlineInterval.VALS.length; j++) {
        context[i][j] = new RMDBContext(Exchange.fromOrdinal(i), KlineInterval.fromOrdinal(j));
      }
    }
  }

  public static final class RMDBContext {

    protected final Exchange exchange;
    protected final KlineInterval klineInterval;

    protected DataSource dataSource;
    protected String tableName;
    protected String selectSql;

    public RMDBContext(Exchange exchange, KlineInterval klineInterval) {
      this.exchange = exchange;
      this.klineInterval = klineInterval;
    }

    public DataSource getDataSource() {
      return dataSource;
    }

    public RMDBContext setDataSource(DataSource dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    public String getTableName() {
      return tableName;
    }

    public RMDBContext setTableName(String tableName) {
      this.tableName = tableName;
      this.selectSql = SELECT_TEMPLATE.replace("<Table>", tableName);
      return this;
    }

    public Exchange getExchange() {
      return exchange;
    }

    public KlineInterval getKlineInterval() {
      return klineInterval;
    }
  }

  public static void registerDatasource(
      final Exchange exchange,
      final KlineInterval interval,
      final DataSource dataSource,
      final TableNameFactory tableNameFactory) {

    INSTANCE
        .context[exchange.ordinal()][interval.ordinal()]
        .setDataSource(dataSource)
        .setTableName(tableNameFactory.name(exchange, interval));
  }

  @Override
  public long cutoffTime(Exchange exchange, KlineInterval interval, String symbol) {

    // 1. pick what table name
    // 2. pick which data source

    final RMDBContext rmdbContext = context[exchange.ordinal()][interval.ordinal()];

    DataSource dataSource = rmdbContext.dataSource;

    if (dataSource == null) {
      logger.warn("FAIL_LOAD_CUT_OFF {},{},{}", exchange, interval, symbol);
      return -1;
    }

    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {

      connection = dataSource.getConnection();
      preparedStatement = connection.prepareStatement(rmdbContext.selectSql);

      preparedStatement.setString(1, symbol);

      final ResultSet rs = preparedStatement.executeQuery();

      while (rs.next()) {
        return rs.getLong(1) - interval.getOverlapGap();
      }

    } catch (Throwable throwable) {

      logger.warn("FAIL_GET_SNAPSHOT_CUT {},{},{}", exchange, interval, throwable);

    } finally {
      closeQuietly(preparedStatement);
      closeQuietly(connection);
    }

    return -1;
  }
}
