package com.gobtx.frontend.ws.jdbc;

import com.gobtx.common.Env;
import com.gobtx.model.enums.Exchange;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

/** Created by Aaron Kuai on 2019/12/9. */
@Profile("jdbc-mkt-service")
@Configuration
@SuppressWarnings("Duplicates")
public class JDBCMarketConfiguration {

  static Logger logger = LoggerFactory.getLogger(JDBCMarketConfiguration.class);

  @Value("${xchange.db.par.path:classpath:database}")
  public String configPathDir;

  @Value("${enable.scan}")
  public boolean enableScan = false;

  @Value("${password.path:/etc/db-prod.properties}")
  public String prodPasswordPath;

  protected final ResourceLoader resourceLoader;

  protected final Environment environment;

  public JDBCMarketConfiguration(
      final ResourceLoader resourceLoader, final Environment environment) {
    this.resourceLoader = resourceLoader;
    this.environment = environment;
  }

  @Bean(name = "primaryDataService")
  public JDBCMarketDataService jdbcMarketDataService(
      @Autowired @Qualifier("marketDataSources") List<DataSourceWrapper> marketDataSources) {

    return new JDBCMarketDataService(marketDataSources);
  }

  // Suppose be read only
  @Bean(name = "marketDataSources")
  @Profile("h2")
  public List<DataSourceWrapper> h2MarketDataSources() {

    final DataSource dataSource =
        new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .setName("qa_mkt_db")
            .addScript("schema.sql")
            .build();

    final List<DataSourceWrapper> res = new ArrayList<>();
    res.add(new DataSourceWrapper(dataSource, null, DBType.H2));
    return res;
  }

  @Bean(name = "marketDataSources")
  @Profile("mysql")
  public List<DataSourceWrapper> mysqlMarketDataSources() throws IOException {

    final List<DataSourceWrapper> res = new ArrayList<>();

    if (enableScan) {
      for (final Exchange exchange : Exchange.VALS) {

        final String path =
            configPathDir
                + FileSystems.getDefault().getSeparator()
                + exchange.getName()
                + FileSystems.getDefault().getSeparator()
                + "market-db-"
                + Env.current().getName()
                + ".properties";

        logger.warn("TRY_LOAD_EXCHANGE_DB_CONFIGURATION {},{}", exchange, path);

        try {
          res.add(initDataSourceAware(path, exchange));
        } catch (Throwable throwable) {
          logger.warn("FAIL_TO_LOAD_EXCHANGE_DB {},{},{}", exchange, path, throwable);
        }
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

  public DataSourceWrapper initDataSourceAware(final String path, final Exchange exchange)
      throws IOException {

    Properties properties = new Properties();

    properties.load(resourceLoader.getResource(path).getInputStream());

    if (Env.isProd()) {
      overwritePassword(
          properties, prodPasswordPath, environment.getProperty("app.user", "th_prod"));
    }

    HikariConfig config = new HikariConfig(properties);

    return new DataSourceWrapper(new HikariDataSource(config), exchange, DBType.MYSQL);
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
}
