package com.gobtx.xchange;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.view.TradeEventView;

import java.util.List;

/** Created by Aaron Kuai on 2019/11/25. */
public interface TradeDataStreamListener {

  /**
   * Update one by one
   *
   * @param data
   * @param exchange
   * @param symbol
   */
  void update(final TradeEventView data, final Exchange exchange, final String symbol);

  /**
   * Update by batch
   *
   * @param dataList
   * @param exchange
   * @param symbol
   */
  void updates(final List<TradeEventView> dataList, final Exchange exchange, final String symbol);
}
