package com.gobtx.xchange.configuration;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;
import com.gobtx.model.enums.KlineIntervalGroup;

import java.util.List;

/** Created by Aaron Kuai on 2019/12/13. */
public class ExchangeIntervalMapper {

  public static final ExchangeIntervalMapper INSTANCE = new ExchangeIntervalMapper();

  protected final IntervalGroupContext[][] groupMapper =
      new IntervalGroupContext[Exchange.VALS.length][];

  private ExchangeIntervalMapper() {

    for (int i = 0; i < Exchange.VALS.length; i++) {

      groupMapper[i] = new IntervalGroupContext[KlineInterval.VALS.length];
      for (int j = 0; j < KlineInterval.VALS.length; j++) {
        groupMapper[i][j] =
            new IntervalGroupContext(Exchange.fromOrdinal(i), KlineInterval.fromOrdinal(j));
      }
    }
  }

  protected static final class IntervalGroupContext {

    final Exchange exchange;
    final KlineInterval interval;
    KlineIntervalGroup group = KlineIntervalGroup.DEAD;

    public IntervalGroupContext(Exchange exchange, KlineInterval interval) {
      this.exchange = exchange;
      this.interval = interval;
    }

    public Exchange getExchange() {
      return exchange;
    }

    public KlineInterval getInterval() {
      return interval;
    }

    public KlineIntervalGroup getGroup() {
      return group;
    }

    public IntervalGroupContext setGroup(KlineIntervalGroup group) {
      this.group = group;
      return this;
    }
  }

  public static void registerIntervalGroup(
      final Exchange exchange, List<KlineIntervalGroup> group) {

    for (KlineIntervalGroup each : group) {
      INSTANCE.groupMapper[exchange.ordinal()][each.getInterval().ordinal()].group = each;
    }
  }

  public KlineIntervalGroup group(final Exchange exchange, final KlineInterval interval) {
    return groupMapper[exchange.ordinal()][interval.ordinal()].group;
  }
}
