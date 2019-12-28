package com.gobtx.xchange.service.meta;

import com.gobtx.model.enums.Exchange;

/** Created by Aaron Kuai on 2019/11/13. */
public interface MetaDataListener<T> {
  void update(T metaData, final Exchange exchange, final Exception exception);
}
