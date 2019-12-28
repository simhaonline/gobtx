package com.gobtx.xchange;

import com.gobtx.common.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

/** Created by Aaron Kuai on 2019/12/12. */
public enum LoadMode {
  INCREMENTAL,
  HISTORY;

  public static LoadMode MODE = INCREMENTAL;

  private static AtomicBoolean started = new AtomicBoolean(false);

  public static LoadMode init() {
    if (started.compareAndSet(false, true)) {
      update(Utils.sysProperty("load_mode", "incremental").trim().toUpperCase());
    }
    return MODE;
  }

  public boolean isHistory() {
    return this == HISTORY;
  }

  private static LoadMode update(final String model) {
    LoadMode got;
    switch (model) {
      case "HISTORY":
        got = HISTORY;
        break;
      default:
        got = INCREMENTAL;
        break;
    }
    MODE = got;
    return MODE;
  }
}
