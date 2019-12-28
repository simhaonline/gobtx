package com.gobtx.frontend.ws.config;

import com.gobtx.common.CPUModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Created by Aaron Kuai on 2019/11/18. */
@Component
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {

  protected CPUModel cpuModel = CPUModel.IO;

  protected boolean shareEventLoop = true;

  protected CPUModel workCpuModel = CPUModel.IO;

  protected int port = 10240;

  protected boolean clientDebug = false;

  protected boolean serverDebug = false;

  public CPUModel getCpuModel() {
    return cpuModel;
  }

  public WebSocketProperties setCpuModel(CPUModel cpuModel) {
    this.cpuModel = cpuModel;
    return this;
  }

  public boolean isShareEventLoop() {
    return shareEventLoop;
  }

  public WebSocketProperties setShareEventLoop(boolean shareEventLoop) {
    this.shareEventLoop = shareEventLoop;
    return this;
  }

  public CPUModel getWorkCpuModel() {
    return workCpuModel;
  }

  public WebSocketProperties setWorkCpuModel(CPUModel workCpuModel) {
    this.workCpuModel = workCpuModel;
    return this;
  }

  public int getPort() {
    return port;
  }

  public WebSocketProperties setPort(int port) {
    this.port = port;
    return this;
  }

  public boolean isClientDebug() {
    return clientDebug;
  }

  public WebSocketProperties setClientDebug(boolean clientDebug) {
    this.clientDebug = clientDebug;
    return this;
  }

  public boolean isServerDebug() {
    return serverDebug;
  }

  public WebSocketProperties setServerDebug(boolean serverDebug) {
    this.serverDebug = serverDebug;
    return this;
  }
}
