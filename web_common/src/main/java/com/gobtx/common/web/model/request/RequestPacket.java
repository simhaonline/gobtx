package com.gobtx.common.web.model.request;

import java.util.Map;

/** Created by Aaron Kuai on 2019/11/19. */
public class RequestPacket {

  protected long requestId;
  protected String path;
  protected Map parameters;

  public long getRequestId() {
    return requestId;
  }

  public RequestPacket setRequestId(long requestId) {
    this.requestId = requestId;
    return this;
  }

  public String getPath() {
    return path;
  }

  public RequestPacket setPath(String path) {
    this.path = path;
    return this;
  }

  public Map getParameters() {
    return parameters;
  }

  public RequestPacket setParameters(Map parameters) {
    this.parameters = parameters;
    return this;
  }

  @Override
  public String toString() {
    return "RequestPacket{"
        + "requestId="
        + requestId
        + ", path='"
        + path
        + '\''
        + ", parameters="
        + parameters
        + '}';
  }
}
