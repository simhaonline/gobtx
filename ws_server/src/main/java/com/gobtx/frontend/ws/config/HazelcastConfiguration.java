package com.gobtx.frontend.ws.config;

import com.gobtx.frontend.ws.hazelcast.HazelcastMarketDataService;
import com.gobtx.frontend.ws.service.MarketDataService;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ConnectionRetryConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

/** Created by Aaron Kuai on 2019/12/4. */
@Configuration
@Profile("hazelcast-mkt-service")
public class HazelcastConfiguration {

  static final Logger logger = LoggerFactory.getLogger(HazelcastConfiguration.class);

  @Autowired ResourceLoader resourceLoader;

  @Value("${hazelcast.members:127.0.0.1}")
  String members;

  // @Value("${hazelcast.config:classpath:hazelcast-client.xml}")
  // String configPath;

  @Value("#{systemProperties['app.instance'] ?: ''}")
  protected String instance;

  @Value("#{systemProperties['app.instance.cnt'] ?: ''}")
  protected String cluster;

  @Bean
  public HazelcastInstance initHazelcast() throws IOException {

    System.setProperty("hazelcast.logging.type", "log4j2");
    System.setProperty("hazelcast.phone.home.enabled", "false");
    System.setProperty("hazelcast.socket.bind.any", "false");
    System.setProperty("java.net.preferIPv4Stack", "true");

    ClientConfig clientConfig = new ClientConfig();
    ConnectionRetryConfig connectionRetryConfig =
        clientConfig.getConnectionStrategyConfig().getConnectionRetryConfig();

    connectionRetryConfig
        .setFailOnMaxBackoff(false)
        .setInitialBackoffMillis(1000)
        .setMaxBackoffMillis(60000)
        .setMultiplier(2)
        .setJitter(0.2)
        .setEnabled(true);

    logger.warn("CACHE_CLIENT_TRY_CONNECT_SERVER {}", members);

    if (members == null || members.trim().length() == 0) {
      logger.error("NO_CACHE_CLIENT_UNDER hazelcast-mkt-service");
      System.exit(1);
    }

    //  Server Side:
    // <map name="template">
    //
    //        <in-memory-format>BINARY</in-memory-format>
    //        <indexes>
    //            <index ordered="true">symbol</index>
    //            <index ordered="true">timeKey</index>
    //            <index ordered="true">openTime</index>
    //        </indexes>
    //        <backup-count>1</backup-count>
    //        <async-backup-count>1</async-backup-count>
    //
    //        <near-cache>
    //            <in-memory-format>OBJECT</in-memory-format>
    //            <serialize-keys>true</serialize-keys>
    //            <cache-local-entries>true</cache-local-entries>
    //            <invalidate-on-change>true</invalidate-on-change>
    //            <eviction eviction-policy="NONE"/>&lt;!&ndash; max-size-policy="ENTRY_COUNT"
    // size="10000"/>&ndash;&gt;
    //        </near-cache>
    //
    //        <map-store enabled="true" initial-mode="LAZY">
    //
    //            <class-name>com.gobtx.xchange.dao.MarketDataLoaderWriter</class-name>
    //            <properties>
    //                <property name="exchange">HUOBI</property>
    //                <property name="interval">m1</property>
    //            </properties>
    //
    //            <write-batch-size>100</write-batch-size>
    //            <write-delay-seconds>10</write-delay-seconds>
    //            <write-coalescing>true</write-coalescing>
    //        </map-store>
    //
    //    </map>

    // <near-cache name="articlesPreloader">
    //        <in-memory-format>OBJECT</in-memory-format>
    //        <invalidate-on-change>false</invalidate-on-change>
    //        <eviction eviction-policy="NONE" max-size-policy="ENTRY_COUNT"/>
    //        <preloader enabled="true" directory="articlesPreloader"
    //                   store-initial-delay-seconds="5" store-interval-seconds="60"/>
    //    </near-cache>

    for (final String member : members.split("[,]")) {
      clientConfig.getNetworkConfig().addAddress(member);
    }

    clientConfig.setInstanceName(instance);

    clientConfig.getGroupConfig().setName("MKT_DATA").setPassword("dev-pass");

    HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

    // Who know what happen?
    client
        .getLifecycleService()
        .addLifecycleListener(
            lifecycleEvent -> logger.warn("HAZELCAST_STATE_CHANGE {}", lifecycleEvent));

    logger.warn("FINISH_INIT_HAZELCAST");

    return client;
  }

  @Bean(name = "secondMarketDataService")
  public MarketDataService marketDataService() {
    return new HazelcastMarketDataService();
  }
}
