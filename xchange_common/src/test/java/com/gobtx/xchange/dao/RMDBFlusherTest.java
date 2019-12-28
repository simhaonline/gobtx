package com.gobtx.xchange.dao;

import org.junit.Test;

import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/15. */
public class RMDBFlusherTest {
  @Test
  public void testPrecision() {

    BigDecimal bigDecimal = new BigDecimal("3.664489950184833");

    System.out.println(bigDecimal.scale());

    BigDecimal another = bigDecimal.setScale(12, BigDecimal.ROUND_UP);

    System.out.println(bigDecimal.toPlainString());
    System.out.println(bigDecimal);

    System.out.println(another.toPlainString());
    System.out.println(another);
  }
}
