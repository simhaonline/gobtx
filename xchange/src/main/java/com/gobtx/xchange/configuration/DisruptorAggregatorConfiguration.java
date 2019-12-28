package com.gobtx.xchange.configuration;

import com.gobtx.xchange.aggregator.MarketStreamAggregator;
import com.gobtx.xchange.configuration.properties.DisruptorProperties;
import com.gobtx.xchange.disruptor.DisruptorConfiguration;
import com.gobtx.xchange.disruptor.DisruptorMarketStreamAggregator;
import com.gobtx.xchange.repository.MarketDataRepository;
import com.lmax.disruptor.BlockingWaitStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Created by Aaron Kuai on 2019/11/13. */
@Configuration
@Profile("disruptor")
public class DisruptorAggregatorConfiguration {

  @Autowired DisruptorProperties disruptorProperties;

  @Autowired MarketDataRepository repository;

  @Bean
  public MarketStreamAggregator aggregator() {

    DisruptorMarketStreamAggregator aggregator;

    DisruptorConfiguration configuration = new DisruptorConfiguration();

    configuration
        .setBufferSize(disruptorProperties.getBufferSize())
        .setDelayInSeconds(disruptorProperties.getDelayInSeconds())
        .setDisruptorFlush(disruptorProperties.isDisruptorFlush())
        .setFlushParallelCnt(disruptorProperties.getCpuModel().count)
        .setProcessParallelCnt(disruptorProperties.getCpuModel().count)
        .setProducerType(disruptorProperties.getProducerType())
        .setWaitStrategy(new BlockingWaitStrategy());

    aggregator = new DisruptorMarketStreamAggregator(configuration, repository);

    // When to start this?

    return aggregator;
  }
}
