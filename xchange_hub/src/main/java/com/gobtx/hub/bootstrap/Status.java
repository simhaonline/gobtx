package com.gobtx.hub.bootstrap;

/** Created by Aaron Kuai on 2019/11/18. */
public enum Status {
  NEW(1),
  INIT(1 << 1),
  CONNECTING(1 << 2),
  CONNECTED(1 << 3),
  AUTH(1 << 4),
  READY(1 << 5),
  FAIL_AUTH(1 << 6),
  SHUT(1 << 7),
  FORCE_SHUT(1 << 8),
  FAIL_CONNECT(1 << 9),
  FAIL_INIT(1 << 10);

  public static int _FAIL_AUTH = 1 << 6;
  public static int _SHUT = 1 << 7;
  public static int _FORCE_SHUT = 1 << 8;
  public static int _FAIL_CONNECT = 1 << 9;

  public static final int DEAD = _FAIL_AUTH | _SHUT | _FORCE_SHUT | _FAIL_CONNECT | 1 << 10;

  protected final int flag;

  Status(int flag) {
    this.flag = flag;
  }

  public int getFlag() {
    return flag;
  }

  public boolean isDead() {
    return (flag & DEAD) > 0;
  }
}
