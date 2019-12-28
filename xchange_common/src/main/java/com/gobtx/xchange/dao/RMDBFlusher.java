package com.gobtx.xchange.dao;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import static com.gobtx.common.Utils.closeQuietly;

/** Created by Aaron Kuai on 2019/11/14. */
public class RMDBFlusher implements Flusher {

  static final int BIG_DECIMAL_LENGTH = 32;
  static final int BIG_DECIMAL_PRECISION = 12;

  static final int BIG_DECIMAL_AMT_PRECISION = 10;

  static final Logger logger = LoggerFactory.getLogger(RMDBFlusher.class);

  protected String selectTemplate =
      "SELECT "
          + "symbol,"
          + "timeKey,"
          + "openTime,"
          + "closeTime,"
          + "open,"
          + "high,"
          + "low,"
          + "close,"
          + "volume,"
          + "amount,"
          + "numberOfTrades "
          + "FROM <Table> "
          + "WHERE symbol = ? AND timeKey =?";

  protected String mysqlSelectTemplate = selectTemplate + " LIMIT 1";

  protected String h2Template =
      "MERGE INTO "
          + "<Table> "
          + "("
          + "symbol,"
          + "timeKey,"
          + "openTime,"
          + "closeTime,"
          + "open,"
          + "high,"
          + "low,"
          + "close,"
          + "volume,"
          + "amount,"
          + "numberOfTrades) "
          + "KEY (symbol,timeKey) "
          + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  protected String mysqlTemplate =
      "INSERT INTO "
          + "<Table> "
          + "("
          + "symbol, "
          + "timeKey, "
          + "openTime, "
          + "closeTime, "
          + "open, "
          + "high, "
          + "low, "
          + "close, "
          + "volume, "
          + "amount, "
          + "numberOfTrades) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,?) "
          + "ON DUPLICATE KEY UPDATE "
          + "openTime = VALUES(openTime), "
          + "closeTime = VALUES(closeTime), "
          + "open = VALUES(open), "
          + "high = VALUES(high), "
          + "low = VALUES(low), "
          + "close = VALUES(close), "
          + "volume = VALUES(volume), "
          + "amount = VALUES(amount), "
          + "numberOfTrades = VALUES(numberOfTrades)";

  protected final String mergerSql;
  protected final String selectSql;

  protected final DataSource dataSource;
  protected final Exchange exchange;
  protected final KlineInterval klineInterval;
  protected final boolean mysql;

  public RMDBFlusher(
      final DataSource dataSource,
      final Exchange exchange,
      final KlineInterval klineInterval,
      final TableNameFactory factory,
      final boolean mysql) {

    this.dataSource = dataSource;
    this.exchange = exchange;
    this.klineInterval = klineInterval;
    this.mysql = mysql;

    selectSql =
        (mysql ? mysqlSelectTemplate : selectTemplate)
            .replace("<Table>", factory.name(exchange, klineInterval));

    mergerSql =
        (mysql ? mysqlTemplate : h2Template)
            .replace("<Table>", factory.name(exchange, klineInterval));
  }

  @Override
  public void batch(final Collection<OHLCData> values) {

    // This distributed in  Exchange  -->  KLine
    // symbol + timeKey

    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {

      connection = dataSource.getConnection();
      preparedStatement = connection.prepareStatement(mergerSql);

      for (final OHLCData value : values) {

        popPrepareStatement(preparedStatement, value);

        preparedStatement.addBatch();
      }

      preparedStatement.executeBatch();

      if (logger.isDebugEnabled()) {
        logger.debug(
            "RMDB_FLUSH_BATCH_SIZE {},{},{}", exchange, klineInterval.code(), values.size());
      }

    } catch (Throwable throwable) {

      // logger.error(ExceptionUtils.getStackTrace(throwable));
      logger.warn(
          "MISSED_SAVE_BATCH_MKT_DATA {},{},{}",
          exchange,
          klineInterval.code(),
          ExceptionUtils.getStackTrace(throwable));

    } finally {
      closeQuietly(preparedStatement);
      closeQuietly(connection);
    }
  }

  private static BigDecimal safeTruncate(final BigDecimal bigDecimal) {

    return bigDecimal.scale() > BIG_DECIMAL_PRECISION
        ? bigDecimal.setScale(BIG_DECIMAL_PRECISION, BigDecimal.ROUND_HALF_DOWN)
        : bigDecimal;
  }

  private static BigDecimal safeTruncate2(final BigDecimal bigDecimal) {

    return bigDecimal.scale() > BIG_DECIMAL_AMT_PRECISION
        ? bigDecimal.setScale(BIG_DECIMAL_AMT_PRECISION, BigDecimal.ROUND_HALF_DOWN)
        : bigDecimal;
  }

  private static void popPrepareStatement(final PreparedStatement ps, final OHLCData value)
      throws SQLException {

    // Some time the decimal is to small cut it
    ps.setString(1, value.getSymbol());
    ps.setLong(2, value.getTimeKey());
    ps.setLong(3, value.getOpenTime());
    ps.setLong(4, value.getCloseTime());
    ps.setBigDecimal(5, safeTruncate(value.getOpen()));
    ps.setBigDecimal(6, safeTruncate(value.getHigh()));
    ps.setBigDecimal(7, safeTruncate(value.getLow()));
    ps.setBigDecimal(8, safeTruncate(value.getClose()));
    ps.setBigDecimal(9, safeTruncate2(value.getVolume()));
    ps.setBigDecimal(10, safeTruncate2(value.getAmount()));
    ps.setLong(11, value.getNumberOfTrades());
  }

  // Merger or update
  @Override
  public void save(final OHLCData value) {

    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {

      connection = dataSource.getConnection();
      preparedStatement = connection.prepareStatement(mergerSql);
      popPrepareStatement(preparedStatement, value);
      preparedStatement.execute();

      if (logger.isDebugEnabled()) {
        logger.debug("RMDB_FLUSH_ONE {},{},{}", exchange, klineInterval.code(), value.getTimeKey());
      }

    } catch (Throwable throwable) {
      logger.warn(
          "MISSED_SAVE_MKT_DATA {},{},{},{}",
          exchange,
          klineInterval,
          value,
          ExceptionUtils.getStackTrace(throwable));

    } finally {
      closeQuietly(preparedStatement);
      closeQuietly(connection);
    }
  }

  @Override
  public OHLCData get(final OHLCKeyData key) {

    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {

      connection = dataSource.getConnection();
      preparedStatement = connection.prepareStatement(selectSql);

      preparedStatement.setString(1, key.getSymbol());
      preparedStatement.setLong(2, key.getTimeKey());

      final ResultSet rs = preparedStatement.executeQuery();

      while (rs.next()) {

        final OHLCDataImpl data = new OHLCDataImpl();
        data.setSymbol(rs.getString(1))
            .setTimeKey(rs.getLong(2))
            .setOpenTime(rs.getLong(3))
            .setCloseTime(rs.getLong(4))
            .setOpen(rs.getBigDecimal(5))
            .setHigh(rs.getBigDecimal(6))
            .setLow(rs.getBigDecimal(7))
            .setClose(rs.getBigDecimal(8))
            .setVolume(rs.getBigDecimal(9))
            .setAmount(rs.getBigDecimal(10))
            .setNumberOfTrades(rs.getLong(11));

        return data;
      }

    } catch (Throwable throwable) {

      logger.warn("MISSED_GET_MKT_DATA {},{},{}", exchange, klineInterval, throwable);

    } finally {
      closeQuietly(preparedStatement);
      closeQuietly(connection);
    }

    return null;
  }

  @Override
  public String toString() {
    return "RMDBFlusher{"
        + "exchange="
        + exchange
        + ", klineInterval="
        + klineInterval
        + ", mysql="
        + mysql
        + '}';
  }
}
