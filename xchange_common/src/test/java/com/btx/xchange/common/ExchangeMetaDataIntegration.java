package com.btx.xchange.common;

import org.junit.BeforeClass;
import org.junit.Test;
import org.knowm.xchange.dto.meta.ExchangeMetaData;

/** Created by Aaron Kuai on 2019/10/30. */
public class ExchangeMetaDataIntegration {

  private static ExchangeMetaData metaData;

  @BeforeClass
  public static void fetchMetaData() throws Exception {
    // Exchange binance = ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class);
    // metaData = binance.getExchangeMetaData();
  }

  @Test
  public void testEthBtcPairMetaData() {
    // CurrencyPairMetaData pairMetaData = metaData.getCurrencyPairs().get(CurrencyPair.ETH_BTC);
    //        assertThat(pairMetaData.getPriceScale()).isEqualByComparingTo(6);
    //        assertThat(pairMetaData.getMinimumAmount()).isEqualByComparingTo("0.001");
    //        assertThat(pairMetaData.getMaximumAmount()).isEqualByComparingTo("100000");
    //        assertThat(pairMetaData.getAmountStepSize()).isEqualByComparingTo("0.001");
  }

  @Test
  public void testGntBtcPairMetaData() {
    // CurrencyPairMetaData pairMetaData =
    //    metaData.getCurrencyPairs().get(new CurrencyPair("GNT/BTC"));

    //        assertThat(pairMetaData.getPriceScale()).isEqualByComparingTo(8);
    //        assertThat(pairMetaData.getMinimumAmount()).isEqualByComparingTo("1");
    //        assertThat(pairMetaData.getMaximumAmount()).isEqualByComparingTo("90000000");
    //        assertThat(pairMetaData.getAmountStepSize()).isEqualByComparingTo("1");
  }
}
