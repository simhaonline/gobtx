package com.gobtx.xchange.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.gobtx.model.enums.Exchange;
import com.gobtx.xchange.service.Version;
import com.gobtx.xchange.service.meta.MetaDataListener;
import com.gobtx.xchange.service.meta.MetaDataService;

public class BinanceMetaDataService implements MetaDataService<ExchangeInfo> {
  @Override
  public void refresh(MetaDataListener<ExchangeInfo> listener) {

    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    BinanceApiAsyncRestClient client = factory.newAsyncRestClient();

    client.getExchangeInfo(response -> listener.update(response, Exchange.BINANCE, null));
  }

  @Override
  public Exchange exchange() {
    return Exchange.BINANCE;
  }

  @Override
  public Version version() {
    return Constants.CURRENT;
  }
}
