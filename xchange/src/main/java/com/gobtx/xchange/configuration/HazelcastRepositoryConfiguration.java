package com.gobtx.xchange.configuration;

import com.gobtx.model.enums.KlineInterval;
import com.gobtx.xchange.hazelcast.HazelcastCacheFactory;
import com.gobtx.xchange.hazelcast.HazelcastMarketDataRepository;
import com.gobtx.xchange.repository.MarketDataRepository;
import com.gobtx.xchange.service.MarketDataService;
import com.hazelcast.config.*;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** Created by Aaron Kuai on 2019/11/13. */
@Configuration
@Profile("hazelcast")
public class HazelcastRepositoryConfiguration {

  static final Logger logger = LoggerFactory.getLogger(HazelcastRepositoryConfiguration.class);

  @Autowired ResourceLoader resourceLoader;

  @Autowired(required = false)
  List<MarketDataService> marketDataServices;

  @Value("${hazelcast.config:classpath:hazelcast-hd-memory.xml}")
  String configPath;

  @Value("${hazelcast.members:NA}")
  String members;

  @Value("#{systemProperties['LOCAL_ADDRESS'] ?: ''}")
  public String localAddress = "localhost";

  @Value("#{systemProperties['MAP_STORE_ENABLE'] ?: 'false'}")
  public boolean mapStoreEnable = false;

  @Bean(destroyMethod = "shutdown")
  protected HazelcastInstance initHazelcast() throws IOException {

    System.setProperty("hazelcast.logging.type", "log4j2");
    System.setProperty("hazelcast.phone.home.enabled", "false");
    System.setProperty("hazelcast.socket.bind.any", "false");
    System.setProperty("java.net.preferIPv4Stack", "true");

    final MemorySize memorySize = MemorySize.parse("1G", MemoryUnit.GIGABYTES);

    final InputStream configInputStream = resourceLoader.getResource(configPath).getInputStream();

    Config config = new XmlConfigBuilder(configInputStream).build();
    config.setLicenseKey("FOR_EVER_YOUNG");

    if (marketDataServices != null) {
      final MapConfig defaultConfig = config.getMapConfig("template");

      marketDataServices.stream()
          .map(it -> it.exchange())
          .forEach(
              it -> {
                for (final KlineInterval interval : KlineInterval.values()) {

                  final String cacheName = it.cacheName(interval);

                  MapConfig mapConfig = new MapConfig(defaultConfig);
                  mapConfig.setName(cacheName);

                  MapStoreConfig storeConfig = mapConfig.getMapStoreConfig();

                  if (mapStoreEnable) {
                    storeConfig.setEnabled(true);
                    storeConfig
                        .setProperty("exchange", it.name())
                        .setProperty("interval", interval.name());
                  } else {
                    storeConfig.setEnabled(false);
                  }

                  logger.warn("ADD_CACHE_CONFIG {}, ENABLE_STORE {}", cacheName, mapStoreEnable);
                  config.addMapConfig(mapConfig);
                }
              });

      config.getCacheConfigs().remove("template");
    }

    NativeMemoryConfig memoryConfig = config.getNativeMemoryConfig();
    if (!memoryConfig.isEnabled()) {
      memoryConfig.setSize(memorySize).setEnabled(true);
      memoryConfig.setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.POOLED);
    }

    // <network>
    //        <port auto-increment="false" port-count="100">5701</port>
    //        <join>
    //            <multicast enabled="false">
    //                <multicast-group>224.2.2.3</multicast-group>
    //                <multicast-port>54327</multicast-port>
    //            </multicast>
    //            <tcp-ip enabled="false" connection-timeout-seconds="30">
    //                <interface>10.1.1.1-10</interface>
    //            </tcp-ip>
    //        </join>
    //        <interfaces enabled="true">
    //            <interface>127.0.0.1</interface>
    //        </interfaces>
    //
    //    </network>

    // AdvancedNetworkConfig works similarly to NetworkConfig however
    // allows finer control of network configuration
    // In order to use AdvancedNetworkConfig, it must be explicitly enabled
    /* config.getAdvancedNetworkConfig().setEnabled(true);

    // configure cluster joiner: disable default multicast joiner, enable & configure TCP/IP
    config.getAdvancedNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

    // May other
    // Only one single nodes may be
    //    config
    //        .getAdvancedNetworkConfig()
    //        .getJoin()
    //        .getTcpIpConfig()
    //        .setEnabled(true)
    //        .setConnectionTimeoutSeconds(30);

    if (members != null && members.trim().length() > 0 && !"NA".equals(members)) {
      config.getAdvancedNetworkConfig().getJoin().getTcpIpConfig().addMember(members);
      logger.warn("CACHE_RUN_IN_CLUSTER {}", members);
    } else {
      logger.warn("CACHE_RUN_IN_SINGLE_MODE");
    }

    ServerSocketEndpointConfig endpointConfig = new ServerSocketEndpointConfig();
    // similarly to NetworkConfig, port, auto-increment and other settings can be configured
    // for each individual endpoint config
    endpointConfig.setPort(5701).setPortAutoIncrement(false).setReuseAddress(true);

    // enable interfaces
    endpointConfig.getInterfaces().setEnabled(true).addInterface(localAddress);

    config.getAdvancedNetworkConfig().setMemberEndpointConfig(endpointConfig);*/

    JoinConfig join = config.getNetworkConfig().getJoin();
    join.getTcpIpConfig().setEnabled(true);
    join.getMulticastConfig().setEnabled(false);

    if (members != null && members.trim().length() > 0 && !"NA".equals(members)) {
      join.getTcpIpConfig().addMember(members);
      logger.warn("CACHE_RUN_IN_CLUSTER {}", members);
    } else {
      logger.warn("CACHE_RUN_IN_SINGLE_MODE");
    }

    final InterfacesConfig interfacesConfig = config.getNetworkConfig().getInterfaces();
    interfacesConfig.setEnabled(true).addInterface(localAddress);

    logger.warn("LOCAL_CACHE_HOST_IN_HOST {}", localAddress);

    final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

    instance
        .getLifecycleService()
        .addLifecycleListener(
            lifecycleEvent -> logger.warn("HAZELCAST_SERVER_STATE {}", lifecycleEvent));

    instance
        .getClientService()
        .addClientListener(
            new ClientListener() {
              @Override
              public void clientConnected(Client client) {
                logger.warn(
                    "CLIENT_JOINED {},{},{},{}",
                    client.getClientType(),
                    client.getLabels(),
                    client.getName(),
                    client.getSocketAddress());
              }

              @Override
              public void clientDisconnected(Client client) {
                logger.warn(
                    "CLIENT_LEFT {},{},{},{}",
                    client.getClientType(),
                    client.getLabels(),
                    client.getName(),
                    client.getSocketAddress());
              }
            });

    return instance;
  }

  // Must grant the DAO all the datasource set up ok
  // @Autowired DAOBootstrap bootstrap;

  @Bean
  public MarketDataRepository marketDataRepository() throws IOException {

    final HazelcastInstance hazelcastInstance = initHazelcast();

    HazelcastCacheFactory factory =
        (exchange, symbol, interval) -> {

          // Change --> internal
          return hazelcastInstance.getMap(exchange.cacheName(interval));
        };

    return new HazelcastMarketDataRepository(factory);
  }
}
