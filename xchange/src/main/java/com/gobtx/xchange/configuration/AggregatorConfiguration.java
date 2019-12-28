package com.gobtx.xchange.configuration;

import com.gobtx.xchange.aggregator.DummyMarketStreamAggregator;
import com.gobtx.xchange.aggregator.MarketStreamAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Created by Aaron Kuai on 2019/11/13. */
@Configuration
public class AggregatorConfiguration {

  @Profile("mock-aggregator")
  @Bean
  public MarketStreamAggregator aggregator() {
    return new DummyMarketStreamAggregator();
  }
}
