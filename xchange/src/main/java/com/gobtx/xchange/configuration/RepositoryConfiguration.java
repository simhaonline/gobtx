package com.gobtx.xchange.configuration;

import com.gobtx.xchange.repository.LocalTestMarketDataRepository;
import com.gobtx.xchange.repository.MarketDataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Created by Aaron Kuai on 2019/11/13. */
@Configuration
public class RepositoryConfiguration {

  @Profile("mock-repository")
  @Bean
  public MarketDataRepository marketDataRepository() {
    return new LocalTestMarketDataRepository();
  }
}
