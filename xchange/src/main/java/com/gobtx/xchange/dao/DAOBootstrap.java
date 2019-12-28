package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static com.gobtx.xchange.dao.RMDBMarketBatchCutoffTimeFinder.registerDatasource;

/** Created by Aaron Kuai on 2019/11/14. */
@Component
public class DAOBootstrap implements InitializingBean {

  protected static final Logger logger = LoggerFactory.getLogger(DAOBootstrap.class);

  @Autowired(required = false)
  @Qualifier("marketDataSources")
  List<DataSourceExchangeAware> marketDataSources;

  public void afterPropertiesSet() throws Exception {
    // If it is dev use one source or multiple source?
    // Pop up the flush

    logger.warn("PREPARE_SETUP_MKT_DATA_SOURCE_CONTEXT");

    if (marketDataSources != null && !marketDataSources.isEmpty()) {

      if (marketDataSources.size() == 1) {

        final DataSourceExchangeAware dataSourceExchangeAware = marketDataSources.get(0);
        registerDefault(dataSourceExchangeAware);
        logger.warn("REGISTER_ONLY_ONE_MARKET_DATA_SOURCE");
      } else {

        // Filter the NULL Exchange this meaning the global
        final List<DataSourceExchangeAware> filtered = new ArrayList<>();
        DataSourceExchangeAware nullExchange = null;

        for (final DataSourceExchangeAware dsea : marketDataSources) {
          if (dsea.getExchange() == null) {
            nullExchange = dsea;
          } else {
            filtered.add(dsea);
          }
        }

        if (nullExchange != null) registerDefault(nullExchange);

        filtered.forEach(
            it -> {
              for (final KlineInterval interval : KlineInterval.VALS) {

                registerDatasource(
                    it.exchange, interval, it.dataSource, TableNameEachExchangeFactory.INSTANCE);

                // This will overwrite the default's
                MarketDataFlusherContext.register(
                    it.exchange,
                    interval,
                    new RMDBFlusher(
                        it.getDataSource(),
                        it.getExchange(),
                        interval,
                        TableNameEachExchangeFactory.INSTANCE,
                        it.isMysql()));
              }
            });
        logger.warn("REGISTER_BY_EXCHANGES");
      }
    } else {
      logger.warn("NO_MARKET_DATA_SOURCE_SET_BE_CAREFULLY");
    }
  }

  /**
   * Default meaning all the Exchange and all the interval share the same DB source
   *
   * @param dataSourceExchangeAware
   */
  private void registerDefault(final DataSourceExchangeAware dataSourceExchangeAware) {

    final DataSource dataSource = dataSourceExchangeAware.getDataSource();

    for (Exchange exchange : Exchange.values()) {

      for (KlineInterval interval : KlineInterval.VALS) {

        registerDatasource(exchange, interval, dataSource, TableNameTogetherFactory.INSTANCE);

        MarketDataFlusherContext.register(
            exchange,
            interval,
            new RMDBFlusher(
                dataSource,
                exchange,
                interval,
                TableNameTogetherFactory.INSTANCE,
                dataSourceExchangeAware.isMysql()));
      }
    }
  }
}
