package com.gobtx.model.domain.hazelcast;

import java.nio.charset.Charset;

/** Created by Aaron Kuai on 2019/11/12. */
public interface HazelcastTypeIdContext {

  Charset US_ASCII = Charset.forName("US-ASCII");

  int OHLC = 1;
  int OHLC_KEY = 2;
}
