package com.gobtx.xchange.huobi;

import com.gobtx.model.enums.Exchange;
import com.gobtx.xchange.service.Version;
import com.gobtx.xchange.service.meta.MetaDataListener;
import com.gobtx.xchange.service.meta.MetaDataService;
import com.huobi.client.AsyncRequestClient;
import com.huobi.client.model.ExchangeInfo;
import org.springframework.stereotype.Component;

import static com.gobtx.xchange.huobi.Constants.CURRENT;

/** Created by Aaron Kuai on 2019/11/13. */
@Component
public class HuobiMetaDataService implements MetaDataService<ExchangeInfo> {

  public void refresh(final MetaDataListener<ExchangeInfo> listener) {

    final AsyncRequestClient client = AsyncRequestClient.create();

    client.getExchangeInfo(
        response -> {
          if (response.succeeded()) {
            listener.update(response.getData(), exchange(), null);
          } else {

            listener.update(null, exchange(), response.getException());
          }
        });
  }

  public Exchange exchange() {
    return Exchange.HUOBI;
  }

  public Version version() {
    return CURRENT;
  }
}
