package com.gobtx.frontend.ws.hub;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Created by Aaron Kuai on 2019/11/20. */
@Component
@ConfigurationProperties(prefix = "hub.server")
public class HubServerProperties {

  // 20240, 20241
  // ip1:port1;ip2:port2;ip3:port3
  protected String hubHosts = "localhost:20240";

  protected int scanFrequent = 90; // 1.5 minute

  public String getHubHosts() {
    return hubHosts;
  }

  public HubServerProperties setHubHosts(String hubHosts) {
    this.hubHosts = hubHosts;
    return this;
  }

  public int getScanFrequent() {
    return scanFrequent;
  }

  public HubServerProperties setScanFrequent(int scanFrequent) {
    this.scanFrequent = scanFrequent;
    return this;
  }
}
