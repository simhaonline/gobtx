package com.gobtx.xchange.configuration.properties;

import com.gobtx.common.CPUModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Created by Aaron Kuai on 2019/11/21. */
@Component
@ConfigurationProperties(prefix = "rpc.server")
public class ServerProperties {

  protected CPUModel cpuModel = CPUModel.IO;

  protected boolean shareEventLoop = true;

  protected CPUModel workCpuModel = CPUModel.IO;

  protected int port = 20240;

  protected boolean clientDebug = false;

  protected boolean serverDebug = false;

  public CPUModel getCpuModel() {
    return cpuModel;
  }

  public ServerProperties setCpuModel(CPUModel cpuModel) {
    this.cpuModel = cpuModel;
    return this;
  }

  public boolean isShareEventLoop() {
    return shareEventLoop;
  }

  public ServerProperties setShareEventLoop(boolean shareEventLoop) {
    this.shareEventLoop = shareEventLoop;
    return this;
  }

  public CPUModel getWorkCpuModel() {
    return workCpuModel;
  }

  public ServerProperties setWorkCpuModel(CPUModel workCpuModel) {
    this.workCpuModel = workCpuModel;
    return this;
  }

  public int getPort() {
    return port;
  }

  public ServerProperties setPort(int port) {
    this.port = port;
    return this;
  }

  public boolean isClientDebug() {
    return clientDebug;
  }

  public ServerProperties setClientDebug(boolean clientDebug) {
    this.clientDebug = clientDebug;
    return this;
  }

  public boolean isServerDebug() {
    return serverDebug;
  }

  public ServerProperties setServerDebug(boolean serverDebug) {
    this.serverDebug = serverDebug;
    return this;
  }
}
