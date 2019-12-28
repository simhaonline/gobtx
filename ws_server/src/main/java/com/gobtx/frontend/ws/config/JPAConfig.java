package com.gobtx.frontend.ws.config;

import com.gobtx.common.Env;
import com.gobtx.frontend.ws.utils.ExternalResourceInitiatorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/** Created by Aaron Kuai on 2019/11/8. */
// Not used yet
@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "customerEntityManagerFactory",
    transactionManagerRef = "customerTransactionManager",
    value = "com.gobtx.frontend.ws.repository")
public class JPAConfig {

  private static final Logger log = LoggerFactory.getLogger(JPAConfig.class);

  @Value("${customer.db.config.path}")
  String customerDbConfigPath;

  final ResourceLoader loader;

  public JPAConfig(ResourceLoader loader) {
    this.loader = loader;
  }

  @Bean(name = "customerDataSource")
  @Profile("h2")
  public DataSource h2CustomerDataSource() {

    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .setName("btx_customers")
        .build();
  }

  // Not used yet

  @Bean("customerDataSource")
  @Profile("mysql")
  public DataSource mysqlCustomerDataSource() throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("INIT_CUSTOMER_DATA_SOURCE {}", customerDbConfigPath);
    }
    return ExternalResourceInitiatorHelper.dataSource(loader, customerDbConfigPath);
  }

  @Autowired
  @Qualifier("customerDataSource")
  DataSource customerDatasource;

  @Bean
  @Primary
  public PlatformTransactionManager customerTransactionManager() {
    return new JpaTransactionManager(customerEntityManagerFactory().getObject());
  }

  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory() {

    HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();

    // Do not do this in production

    if (Env.isDev()) {

      jpaVendorAdapter.setGenerateDdl(true);
      log.warn("NO_PRODUCT_GENERATE_DDL_AUTO_FOR_CUSTOMER");
    }

    LocalContainerEntityManagerFactoryBean factoryBean =
        new LocalContainerEntityManagerFactoryBean();

    factoryBean.setDataSource(customerDatasource);
    factoryBean.setJpaVendorAdapter(jpaVendorAdapter);

    factoryBean.setPackagesToScan("com.gobtx.model.persist");

    return factoryBean;
  }
}
