package com.gobtx.model.enums;

import java.nio.charset.StandardCharsets;

/**
 * Created by Aaron Kuai on 2019/11/11.
 *
 * <p>this is a bit hard code, but in fact this should move to the configuration server
 */
public enum Exchange {
  MOCK("mock", "Mock Exchange", 0),
  BINANCE("binance", "Binance Exchange", 1),
  HUOBI("huobi", "Huobi Exchange", 2);

  protected final String name;
  protected final byte[] nameBytes;
  protected final String description;
  protected final int globalIndex;

  protected final String[] cacheKey;
  protected final String[] tableName;

  Exchange(String name, String description, int globalIndex) {
    this.name = name;
    this.description = description;
    this.globalIndex = globalIndex;

    cacheKey = new String[KlineInterval.VALS.length];
    tableName = new String[KlineInterval.VALS.length];

    for (int i = 0; i < KlineInterval.values().length; i++) {
      cacheKey[i] = name.concat("_" + KlineInterval.VALS[i].code());
      tableName[i] = name.concat("_" + KlineInterval.VALS[i].getTableSuffix());
    }

    nameBytes = ("\"" + name() + "\"").getBytes(StandardCharsets.UTF_8);
  }

  public byte[] getNameBytes() {
    return nameBytes;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getGlobalIndex() {
    return globalIndex;
  }

  public String cacheName(final KlineInterval klineInterval) {
    return cacheKey[klineInterval.ordinal()];
  }

  public String tableName(final KlineInterval interval) {
    return tableName[interval.ordinal()];
  }

  /** Enumerated values. */
  public static final Exchange[] VALS = values();

  public static Exchange fromOrdinal(int ord) {
    return ord >= 0 && ord < VALS.length ? VALS[ord] : null;
  }
}
