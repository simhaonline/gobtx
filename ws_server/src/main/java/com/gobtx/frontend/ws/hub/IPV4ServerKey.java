package com.gobtx.frontend.ws.hub;

import java.net.InetSocketAddress;
import java.util.Objects;

/** Created by Aaron Kuai on 2019/11/20. */
public class IPV4ServerKey implements ServerKey {

  protected final String ip;
  protected final int port;

  public IPV4ServerKey(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  @Override
  public InetSocketAddress address() {
    return new InetSocketAddress(ip, port);
  }

  public String getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IPV4ServerKey that = (IPV4ServerKey) o;
    return port == that.port && Objects.equals(ip, that.ip);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, port);
  }

  @Override
  public String toString() {
    return "IPV2ServerKey{" + "ip='" + ip + '\'' + ", port=" + port + '}';
  }
}
