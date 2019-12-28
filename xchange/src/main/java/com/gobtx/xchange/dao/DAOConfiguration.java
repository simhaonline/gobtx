package com.gobtx.xchange.dao;

import com.gobtx.common.Env;
import com.gobtx.model.enums.Exchange;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Created by Aaron Kuai on 2019/11/14. */
@Configuration
public class DAOConfiguration {

  static Logger logger = LoggerFactory.getLogger(DAOConfiguration.class);

  @Value("${xchange.db.par.path:classpath:database}")
  public String configPathDir;

  @Value("${enable.exchanges}")
  public String enableExchanges;

  @Value("${enable.scan}")
  public boolean enableScan = false;

  @Value("${password.path:/etc/db-prod.properties}")
  public String prodPasswordPath;

  protected final ResourceLoader resourceLoader;

  protected final Environment environment;

  public DAOConfiguration(ResourceLoader resourceLoader, Environment environment) {
    this.resourceLoader = resourceLoader;
    this.environment = environment;
  }

  @Bean(name = "marketDataSources")
  @Profile("h2")
  List<DataSourceExchangeAware> h2MarketDataSources() {

    final DataSource dataSource =
        new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .setName("qa_mkt_db")
            .addScript("schema.sql")
            .build();

    final List<DataSourceExchangeAware> res = new ArrayList<>();
    res.add(new DataSourceExchangeAware(dataSource, null, false));
    return res;
  }

  @Bean(name = "marketDataSources")
  @Profile("mysql")
  List<DataSourceExchangeAware> mysqlMarketDataSources() throws IOException {

    if (Env.isProd()) {
      enableScan = true;
    }

    final List<DataSourceExchangeAware> res = new ArrayList<>();

    if (enableScan) {
      for (final String ex : enableExchanges.split(",")) {

        final Exchange exchange = Exchange.valueOf(ex.toUpperCase());

        final String path =
            configPathDir
                + FileSystems.getDefault().getSeparator()
                + exchange.getName()
                + FileSystems.getDefault().getSeparator()
                + "market-db-"
                + Env.current().getName()
                + ".properties";

        logger.warn("TRY_LOAD_EXCHANGE_DB_CONFIGURATION {},{}", exchange, path);

        res.add(initDataSourceAware(path, exchange));
      }
    } else {

      // pop up the generic
      final String path =
          configPathDir
              + FileSystems.getDefault().getSeparator()
              + "market-db-"
              + Env.current().getName()
              + ".properties";

      logger.warn("TRY_LOAD_GENERIC_DB_CONFIGURATION {}", path);
      res.add(initDataSourceAware(path, null));
    }
    return res;
  }

  public DataSourceExchangeAware initDataSourceAware(final String path, final Exchange exchange)
      throws IOException {

    Properties properties = new Properties();

    properties.load(resourceLoader.getResource(path).getInputStream());

    if (Env.isProd()) {
      overwritePassword(
          properties, prodPasswordPath, environment.getProperty("app.user", "th_prod"));
    }

    HikariConfig config = new HikariConfig(properties);

    return new DataSourceExchangeAware(new HikariDataSource(config), exchange, true);
  }

  private static void overwritePassword(
      Properties properties, String prodPasswordPath, String userName) throws IOException {

    if (prodPasswordPath == null || prodPasswordPath.isEmpty()) {
      prodPasswordPath = "/etc/db-prod.properties";
      logger.warn("DEFAULT_PASSWORD_PATH");
    }

    // final String userName = environment.getProperty("app.user", "th_prod");
    Properties properties2 = new Properties();
    FileInputStream stream = new FileInputStream(new File(prodPasswordPath));
    properties2.load(stream);
    final String pd = (String) properties2.get(userName);

    if (pd == null || pd.length() < 0) {
      throw new IllegalStateException("No " + prodPasswordPath + " contract your system admin");
    }
    properties.put("username", userName);
    properties.put("password", pd);
    logger.warn("PRODUCTION_DB_PASSWORD_OVERWRITTEN_SUCCESS");
  }

  @Bean
  public DAOBootstrap daoBootstrap() {
    return new DAOBootstrap();
  }

  @Bean
  public MarketBatchCutoffTimeFinder finder() {
    return AlwaysFromVeryBeginMarketBatchCutoffTimeFinder.INSTANCE;
    // return RMDBMarketBatchCutoffTimeFinder.INSTANCE;
  }
}
