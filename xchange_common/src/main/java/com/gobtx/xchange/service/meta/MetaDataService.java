package com.gobtx.xchange.service.meta;

import com.gobtx.xchange.service.BaseService;

/** Created by Aaron Kuai on 2019/11/13. */
public interface MetaDataService<T> extends BaseService {

  /**
   * Try to refresh the exchange's meta data information There are too much different so not sure
   * which kind data structure should be applied
   */
  void refresh(final MetaDataListener<T> listener);
}
