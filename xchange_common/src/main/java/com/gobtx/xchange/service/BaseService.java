package com.gobtx.xchange.service;

import com.gobtx.model.enums.Exchange;

/** Created by Aaron Kuai on 2019/11/13. */
public interface BaseService {
  Exchange exchange();

  Version version();
}
