package com.gobtx.frontend.ws.jdbc;

import cn.hutool.core.lang.Pair;
import com.gobtx.common.executor.GlobalExecutorService;
import com.gobtx.common.executor.GlobalScheduleService;
import com.gobtx.frontend.ws.push.LocalMarketDataLastSnapshotter;
import com.gobtx.frontend.ws.service.MarketDataService;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.dto.OHLCWithExchangeAndInternalData;
import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;
import com.gobtx.model.view.OHLCWithExchangeAndIntervalView;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.gobtx.common.Utils.closeQuietly;

/** Created by Aaron Kuai on 2019/12/9. */
public class JDBCMarketDataService extends LocalMarketDataLastSnapshotter
    implements MarketDataService {

  static final Logger logger = LoggerFactory.getLogger(JDBCMarketDataService.class);

  protected final List<DataSourceWrapper> dataSourceList;

  // TODO this is a bug in this solution  may be hole for different kind symbols

  // Symbol -->  OHLC tree
  final Map<String, TreeSet<OHLCWithExchangeAndIntervalView>>[][] localCacheMap =
      new ConcurrentHashMap[Exchange.VALS.length][];

  protected final Function<String, TreeSet<OHLCWithExchangeAndIntervalView>> TREE_FACTORY =
      new Function<String, TreeSet<OHLCWithExchangeAndIntervalView>>() {
        @Override
        public TreeSet<OHLCWithExchangeAndIntervalView> apply(String s) {
          return new TreeSet<>(sorter);
        }
      };

  final long[][] lastSnapshotTime = new long[Exchange.VALS.length][];

  final AtomicBoolean started = new AtomicBoolean(false);
  final AtomicBoolean refreshing = new AtomicBoolean(true);

  protected static int MAX_REFRESH_MINUTES = 15;

  protected Runnable refreshJob;

  protected ScheduledFuture scheduledFuture;

  protected Comparator<OHLCWithExchangeAndIntervalView> sorter =
      (o1, o2) -> {
        final long res = o1.getOpenTime() - o2.getOpenTime();
        return res > 0 ? 1 : (res == 0 ? 0 : -1);
      };

  public JDBCMarketDataService(final List<DataSourceWrapper> dataSourceList) {
    this.dataSourceList = dataSourceList;
    for (int i = 0; i < Exchange.VALS.length; i++) {

      localCacheMap[i] = new ConcurrentHashMap[KlineInterval.VALS.length];
      lastSnapshotTime[i] = new long[KlineInterval.VALS.length];

      for (int j = 0; j < KlineInterval.VALS.length; j++) {
        localCacheMap[i][j] = new ConcurrentHashMap<>(16);
        lastSnapshotTime[i][j] = -1;
      }
    }
  }

  private void singleRefresh() {

    final DataSource dataSource = dataSourceList.get(0).dataSource;

    refreshJob =
        () -> {
          final CountDownLatch latch =
              new CountDownLatch((Exchange.VALS.length - 1) * KlineInterval.VALS.length);

          for (Exchange exchange : Exchange.VALS) {

            if (exchange == Exchange.MOCK) continue;

            for (KlineInterval interval : KlineInterval.VALS) {
              GlobalExecutorService.INSTANCE.submit(
                  () -> {
                    kickPopup(
                        exchange,
                        interval,
                        sharedTableName(exchange, interval),
                        dataSource,
                        cutTime(exchange, interval),
                        latch);
                  });
            }
          }
          try {
            latch.await(MAX_REFRESH_MINUTES, TimeUnit.MINUTES);
          } catch (Throwable throwable) {
            logger.warn("FAIL_INIT1_LOCAL_CACHE {}", ExceptionUtils.getStackTrace(throwable));
          } finally {
            refreshing.set(false);
          }
        };
  }

  private long cutTime(final Exchange exchange, final KlineInterval interval) {

    return lastSnapshotTime[exchange.ordinal()][interval.ordinal()] <= 0
        ? System.currentTimeMillis() - interval.getHistoryLength()
        : lastSnapshotTime[exchange.ordinal()][interval.ordinal()];
  }

  private void multiRefresh() {

    final Map<Exchange, DataSourceWrapper> exchangeDataSourceWrapperMap = new HashMap<>();
    DataSourceWrapper defDS = null;
    for (DataSourceWrapper ds : dataSourceList) {

      if (ds.getExchange() == null) {
        defDS = ds;
      } else {
        exchangeDataSourceWrapperMap.put(ds.getExchange(), ds);
      }
    }
    final Set<Exchange> missed = new HashSet<>();
    for (Exchange ex : Exchange.VALS) {
      if (Exchange.MOCK != ex && !exchangeDataSourceWrapperMap.containsKey(ex)) missed.add(ex);
    }

    if (missed.size() > 0 && defDS == null) {
      throw new IllegalStateException("Exchange source no enough, no default datasource");
    }

    final DataSourceWrapper dds = defDS;

    refreshJob =
        () -> {
          final CountDownLatch latch =
              new CountDownLatch((Exchange.VALS.length - 1) * KlineInterval.VALS.length);

          for (DataSourceWrapper value : exchangeDataSourceWrapperMap.values()) {

            for (final KlineInterval interval : KlineInterval.VALS) {

              GlobalExecutorService.INSTANCE.submit(
                  () -> {
                    kickPopup(
                        value.exchange,
                        interval,
                        interval.getTableSuffix(),
                        value.dataSource,
                        cutTime(value.exchange, interval),
                        latch);
                  });
            }
          }
          // Wait all those done?
          // What about the global

          if (!missed.isEmpty()) {
            // The last generated one
            // Load all from the default things
            for (final Exchange exchange : missed) {
              for (final KlineInterval interval : KlineInterval.VALS) {

                GlobalExecutorService.INSTANCE.submit(
                    () -> {
                      kickPopup(
                          exchange,
                          interval,
                          sharedTableName(exchange, interval),
                          dds.dataSource,
                          cutTime(exchange, interval),
                          latch);
                    });
              }
            }
          }

          try {
            latch.await(MAX_REFRESH_MINUTES, TimeUnit.SECONDS);
          } catch (Throwable throwable) {
            logger.error("FAIL_WAIT_INIT_DONE {}", throwable);
          } finally {
            refreshing.set(false);
          }
        };
  }

  @Override
  public void start() {
    if (started.compareAndSet(false, true)) {
      logger.warn("PREPARE_START_LOCAL_JDBC_BACKED_SERVICE");

      // 1. Load all the data
      // 2. Start watch dogs

      if (this.dataSourceList.size() == 1) {
        // Only one so the loader will be very simple
        logger.warn("SINGLE_DATA_SOURCE_PREPARE");
        singleRefresh();
      } else {
        // Each exchanges
        logger.warn("MULTI_DATA_SOURCE_PREPARE");
        multiRefresh();
      }

      refreshJob.run();

      scheduledFuture =
          GlobalScheduleService.INSTANCE.scheduleAtFixedRate(
              () -> {
                if (refreshing.compareAndSet(false, true)) {
                  refreshJob.run();
                } else {
                  logger.warn("LAST_ROUND_JOB_STILL_RUNNING_SO_IGNORE");
                }
              },
              1,
              1,
              TimeUnit.MINUTES);
    }
  }

  private Pair<Integer, Long> kickPopup(
      final Exchange exchange,
      final KlineInterval interval,
      final String table,
      final DataSource dataSource,
      final long cutTime,
      final CountDownLatch latch) {

    final String select = selectTemplate.replace("<Table>", table);

    final Map<String, TreeSet<OHLCWithExchangeAndIntervalView>> target =
        localCacheMap[exchange.ordinal()][interval.ordinal()];

    try {

      final long start = System.currentTimeMillis();

      final Pair<Integer, Long> res =
          popUpData(target, exchange, interval, dataSource, cutTime, select);

      if (res.getValue() > lastSnapshotTime[exchange.ordinal()][interval.ordinal()]) {
        lastSnapshotTime[exchange.ordinal()][interval.ordinal()] = res.getValue();
      }

      final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:MM");

      logger.warn(
          "PRE_FETCH {},{},  SIZE:{},COST:{}, [{} ~ {}]",
          exchange,
          interval,
          res.getKey(),
          (System.currentTimeMillis() - start) / 1000,
          formatter.format(new Date(cutTime)),
          formatter.format(new Date(res.getValue())));

      return res;
    } catch (Throwable throwable) {
      logger.error(
          "FAIL_CHECK_LAST_SNAPSHOT {},{},{},{}",
          exchange,
          interval,
          table,
          ExceptionUtils.getStackTrace(throwable));
      throw throwable;
    } finally {
      if (latch != null) {
        latch.countDown();
      }
    }
  }

  private Pair<Integer, Long> popUpData(
      final Map<String, TreeSet<OHLCWithExchangeAndIntervalView>> target,
      final Exchange exchange,
      final KlineInterval interval,
      final DataSource dataSource,
      final long cutTime,
      final String sql) {

    int totalLoaded = 0;
    long maxTimestamp = -1;
    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {

      connection = dataSource.getConnection();
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setLong(1, cutTime);

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

        if (data.getOpenTime() > maxTimestamp) {
          maxTimestamp = data.getOpenTime();
        }

        target
            .computeIfAbsent(data.getSymbol(), TREE_FACTORY)
            .add(new OHLCWithExchangeAndInternalData(exchange, interval, data));
        totalLoaded++;
      }

    } catch (Throwable throwable) {
      logger.warn("FAIL_POP_UP_MKT_DATA {},{},{}", exchange, interval, throwable);
    } finally {
      closeQuietly(preparedStatement);
      closeQuietly(connection);
    }

    return new Pair<>(totalLoaded, maxTimestamp);
  }

  private static final String sharedTableName(
      final Exchange exchange, final KlineInterval interval) {
    return exchange.tableName(interval);
  }

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
          + "WHERE openTime >= ?";

  @Override
  public void stop() {

    if (scheduledFuture != null) {
      try {
        scheduledFuture.cancel(true);
      } catch (Throwable throwable) {

      }
    }
  }

  // int MAX_SIZE = 1000;

  @Override
  public List<? extends OHLCView> data(
      String symbol,
      KlineInterval interval,
      Exchange exchange,
      long startTime,
      long endTime,
      boolean first) {
    // If not ready suppose not to load it ?
    final Map<String, TreeSet<OHLCWithExchangeAndIntervalView>> row =
        localCacheMap[exchange.ordinal()][interval.ordinal()];

    TreeSet<OHLCWithExchangeAndIntervalView> target = row.computeIfAbsent(symbol, TREE_FACTORY);

    if (target.isEmpty()) return Collections.EMPTY_LIST;

    // is this tree desc or asc
    long s = startTime, e = endTime;
    if (startTime > endTime) {
      // Start is the bigger one and end is the little one
      s = endTime;
      e = startTime;
    }

    // this is reverse as the order of the data is ASC

    final StubOHLCWithExchangeAndIntervalView start = new StubOHLCWithExchangeAndIntervalView(s);
    final StubOHLCWithExchangeAndIntervalView end = new StubOHLCWithExchangeAndIntervalView(e);

    final NavigableSet<OHLCWithExchangeAndIntervalView> result =
        target.subSet(start, true, end, true);

    final List<OHLCWithExchangeAndIntervalView> res = new ArrayList<>(result.size());
    if (!result.isEmpty()) {
      final Iterator<OHLCWithExchangeAndIntervalView> iterator = result.iterator();
      while (iterator.hasNext()) {
        res.add(iterator.next());
      }
      return res;
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  public OHLCWithExchangeAndIntervalView handle(final OHLCWithExchangeAndIntervalView data) {

    OHLCWithExchangeAndIntervalView updated = super.handle(data);

    localCacheMap[data.getExchange().ordinal()][data.getInterval().ordinal()]
        .computeIfAbsent(data.getSymbol(), TREE_FACTORY)
        .add(updated);

    return updated;
  }

  @Override
  public int order() {
    return 0;
  }
}
