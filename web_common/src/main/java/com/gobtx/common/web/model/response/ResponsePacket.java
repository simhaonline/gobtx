package com.gobtx.common.web.model.response;

import java.util.Map;

/** Created by Aaron Kuai on 2019/11/19. */
public class ResponsePacket {

  protected int code;
  protected long requestId;
  protected String errorMsg;
  protected String path;
  protected Map data;

  public int getCode() {
    return code;
  }

  public ResponsePacket setCode(int code) {
    this.code = code;
    return this;
  }

  public long getRequestId() {
    return requestId;
  }

  public ResponsePacket setRequestId(long requestId) {
    this.requestId = requestId;
    return this;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public ResponsePacket setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
    return this;
  }

  public String getPath() {
    return path;
  }

  public ResponsePacket setPath(String path) {
    this.path = path;
    return this;
  }

  public Map getData() {
    return data;
  }

  public ResponsePacket setData(Map data) {
    this.data = data;
    return this;
  }

  @Override
  public String toString() {
    return "ResponsePacket{"
        + "code="
        + code
        + ", requestId="
        + requestId
        + ", errorMsg='"
        + errorMsg
        + '\''
        + ", path='"
        + path
        + '\''
        + ", data="
        + data
        + '}';
  }
}
