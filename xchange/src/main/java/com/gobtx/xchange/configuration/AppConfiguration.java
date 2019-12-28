package com.gobtx.xchange.configuration;

import com.gobtx.xchange.Bootstrap;
import com.gobtx.xchange.dao.DAOConfiguration;
import com.gobtx.xchange.smock.MockMarketDataService;
import org.springframework.context.annotation.*;

/** Created by Aaron Kuai on 2019/11/13. */
@Configuration
@PropertySources({
  @PropertySource("classpath:application/common.properties"),
  @PropertySource("classpath:application/${app.env:dev}.properties")
})
@Import({DAOConfiguration.class})
@ComponentScan(basePackages = "com.gobtx.xchange.configuration")
public class AppConfiguration {

  @Bean
  Bootstrap bootstrap() {
    return new Bootstrap();
  }

  @Profile("mock-market")
  @Bean
  MockMarketDataService mockMarketDataService() {
    return new MockMarketDataService();
  }
}
