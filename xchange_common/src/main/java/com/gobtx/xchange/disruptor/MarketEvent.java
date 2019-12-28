package com.gobtx.xchange.disruptor;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.view.OHLCView;

import java.util.Calendar;

/** Created by Aaron Kuai on 2019/11/11. */
public class MarketEvent {

  protected int segmentId;

  protected OHLCView data;

  protected KlineInterval type;

  protected Exchange exchange;

  protected String symbol;

  protected Calendar calendar;

  protected boolean derived;

  public MarketEvent reset(
      final int segmentId,
      final OHLCView data,
      final KlineInterval type,
      final Exchange exchange,
      final String symbol,
      final Calendar calendar,
      final boolean derived) {

    this.segmentId = segmentId;
    this.data = data;
    this.type = type;
    this.exchange = exchange;
    this.symbol = symbol;
    this.calendar = calendar;
    this.derived = derived;

    return this;
  }

  public boolean isDerived() {
    return derived;
  }

  /**
   * This is the dead event to trigger some thing not biz related of the ring buffer like the purge
   * job or submit of the transaction etc so do not use it by mistake
   *
   * @return
   */
  public boolean isCheckPointEvent() {
    return -2 == segmentId || this == REGULAR_EVENT;
  }

  protected static final MarketEvent REGULAR_EVENT =
      new MarketEvent() {

        @Override
        public MarketEvent reset(
            final int segmentId,
            final OHLCView data,
            final KlineInterval type,
            final Exchange exchange,
            final String symbol,
            final Calendar calendar,
            final boolean derived) {
          throw new IllegalStateException("this is regular event do not try to fuck trigger me");
        }
      };

  public MarketEvent posion() {
    segmentId = -1;
    return this;
  }

  public MarketEvent regular() {
    segmentId = -2;
    return this;
  }

  public boolean isDeadEvent() {
    return -1 == segmentId || this == POISON_PILL;
  }

  protected static final MarketEvent POISON_PILL =
      new MarketEvent() {
        public MarketEvent reset(
            final int segmentId,
            final OHLCView data,
            final KlineInterval type,
            final Exchange exchange,
            final String symbol,
            final Calendar calendar,
            final boolean derived) {
          throw new IllegalStateException("this is dead event do not try to fuck trigger me");
        }
      };
}
