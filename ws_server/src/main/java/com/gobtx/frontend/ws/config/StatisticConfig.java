package com.gobtx.frontend.ws.config;

import com.gobtx.model.statistic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/** Created by Aaron Kuai on 2019/12/23. */
@Configuration
@Import({BinanceTrade24HProvider.class, HuobiTrade24HProvider.class})
public class StatisticConfig {

  static final Logger logger = LoggerFactory.getLogger(StatisticConfig.class);

  @Autowired(required = false)
  List<Trade24HProvider> providers;

  @Bean
  public Trade24HManager manager() {
    Trade24HManager manager = Trade24HManager.INSTANCE;
    manager.start(providers);
    return manager;
  }

  @Bean
  public SymbolProvider provider() {
    return new LocalSymbolProvider();
  }
}
