package com.gobtx.model.statistic;

import org.junit.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_UP;

/** Created by Aaron Kuai on 2019/12/23. */
public class BinanceTrade24HProviderTest {

  @Test
  public void doFetch() {

    final String price = "132.23213";
    BigDecimal bd = new BigDecimal(price);
    System.out.println(bd.scale());

    final double what = 123d / 13233d;

    BigDecimal bd2 = new BigDecimal(what).setScale(bd.scale(), ROUND_UP);

    System.out.println(what + "   " + bd2);
  }
}
