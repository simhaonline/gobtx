package com.gobtx.xchange.dao;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import org.junit.Test;

/** Created by Aaron Kuai on 2019/11/14. */
public class MarketTableDDLTemplateTest {

  @Test
  public void table() {

    for (Exchange exchange : Exchange.values()) {

      System.out.println("\n\n--------------------\n\n");

      for (final KlineInterval interval : KlineInterval.values()) {

        System.out.println("\n---\n");
        System.out.println(
            MarketTableDDLTemplate.table(
                TableNameTogetherFactory.INSTANCE.name(exchange, interval)));
      }
    }
  }

  @Test
  public void generateH2TableSchema() {

    StringBuffer res = new StringBuffer();

    for (Exchange exchange : Exchange.values()) {

      for (final KlineInterval interval : KlineInterval.values()) {

        res.append(
            MarketTableDDLTemplate.table(
                TableNameTogetherFactory.INSTANCE.name(exchange, interval)));
      }
    }
    System.out.println(res.toString());
  }
}
