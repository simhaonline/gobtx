package com.gobtx.xchange.statistic;

import com.gobtx.model.enums.Exchange;
import com.gobtx.model.enums.KlineInterval;

import java.text.SimpleDateFormat;
import java.util.*;

/** Created by Aaron Kuai on 2019/12/12. */
public class BatchLoadStatistic {

  protected final Exchange exchange;

  protected final long startTime;

  protected long endTime;

  protected Map<String, IntervalLoadStatistic> loadStatisticMap = new HashMap<>();

  public BatchLoadStatistic setEndTime(long endTime) {
    this.endTime = endTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public static class IntervalLoadStatistic {

    protected final KlineInterval interval;
    protected final String symbol;
    protected final long startTime;
    protected long endTime;

    protected long cutTime;
    protected long firstOpenTime;
    protected long lastOpenTime;

    protected int queryCount;
    protected long batchSize;

    public IntervalLoadStatistic(KlineInterval interval, String symbol, long startTime) {
      this.interval = interval;
      this.symbol = symbol;
      this.startTime = startTime;
    }

    public IntervalLoadStatistic(KlineInterval interval, String symbol) {
      this.interval = interval;
      this.symbol = symbol;
      this.startTime = System.currentTimeMillis();
    }

    public KlineInterval getInterval() {
      return interval;
    }

    public long getStartTime() {
      return startTime;
    }

    public long getEndTime() {
      return endTime;
    }

    public IntervalLoadStatistic setEndTime(long endTime) {
      this.endTime = endTime;
      return this;
    }

    public long getFirstOpenTime() {
      return firstOpenTime;
    }

    public IntervalLoadStatistic setFirstOpenTime(long firstOpenTime) {
      this.firstOpenTime = firstOpenTime;
      return this;
    }

    public long getLastOpenTime() {
      return lastOpenTime;
    }

    public IntervalLoadStatistic setLastOpenTime(long lastOpenTime) {
      this.lastOpenTime = lastOpenTime;
      return this;
    }

    public int getQueryCount() {
      return queryCount;
    }

    public IntervalLoadStatistic setQueryCount(int queryCount) {
      this.queryCount = queryCount;
      return this;
    }

    public long getBatchSize() {
      return batchSize;
    }

    public IntervalLoadStatistic setBatchSize(long batchSize) {
      this.batchSize = batchSize;
      return this;
    }

    public IntervalLoadStatistic plusQueryCount() {
      queryCount++;
      return this;
    }

    public IntervalLoadStatistic plusBatchSize(final int size) {
      this.batchSize += size;
      return this;
    }

    public String getSymbol() {
      return symbol;
    }

    public long getCutTime() {
      return cutTime;
    }

    public IntervalLoadStatistic setCutTime(long cutTime) {
      this.cutTime = cutTime;
      return this;
    }

    @Override
    public String toString() {

      final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:MM");

      return String.format(
          "%-16s %-6s %-18s %-18s %-6s %-18s %-18s %-18s %-4s %4s%n",
          symbol,
          interval,
          formatter.format(new Date(startTime)),
          formatter.format(new Date(endTime)),
          String.valueOf((endTime - startTime) / 60_000),
          formatter.format(new Date(cutTime)),
          formatter.format(new Date(firstOpenTime)),
          formatter.format(new Date(lastOpenTime)),
          String.valueOf(queryCount),
          String.valueOf(batchSize));
    }
  }

  public BatchLoadStatistic(Exchange exchange, long startTime) {
    this.exchange = exchange;
    this.startTime = startTime;
  }

  public BatchLoadStatistic(Exchange exchange) {
    this.exchange = exchange;
    this.startTime = System.currentTimeMillis();
  }

  public IntervalLoadStatistic statistic(final KlineInterval interval, final String symbol) {
    return loadStatisticMap.computeIfAbsent(
        interval.name() + "." + symbol, k -> new IntervalLoadStatistic(interval, symbol));
  }

  @Override
  public String toString() {

    final StringBuffer stringBuffer = new StringBuffer();

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:MM:ss");

    stringBuffer
        .append(
            "\n\nBATCH STATISTIC\n---------------------------------------------------------------------------------------------------------------------------------------\n")
        .append(exchange)
        .append("\n")
        .append(formatter.format(new Date(startTime)))
        .append("\n")
        .append(formatter.format(new Date(endTime)))
        .append("\n")
        .append((endTime - startTime) / 60_000) // in second
        .append(
            " minutes\n---------------------------------------------------------------------------------------------------------------------------------------\n");

    // String.format(
    //          "%-16s %-6s %-18s %-18s %-6s %-18s %-18s %-18s %-4s %4s%n",
    //          symbol,
    //          interval,
    //          formatter.format(new Date(startTime)),
    //          formatter.format(new Date(endTime)),
    //          String.valueOf((endTime - startTime) / 60_000),
    //          formatter.format(new Date(cutTime)),
    //          formatter.format(new Date(firstOpenTime)),
    //          formatter.format(new Date(lastOpenTime)),
    //          String.valueOf(queryCount),
    //          String.valueOf(batchSize));

    stringBuffer.append(
        String.format(
            "%-16s %-6s %-18s %-18s %-6s %-18s %-18s %-18s %-4s %4s%n",
            "Symbol", "Int", "Start", "End", "Cost", "Cut", "First", "Last", "Rnd", "Cnt"));

    final List<IntervalLoadStatistic> res =
        new ArrayList<BatchLoadStatistic.IntervalLoadStatistic>(loadStatisticMap.values());

    res.sort(
        (o1, o2) -> {
          int res1 = o1.getSymbol().compareTo(o2.getSymbol());
          if (res1 == 0) {
            return o1.getInterval().ordinal() - o2.getInterval().ordinal();
          }

          return res1;
        });

    res.forEach(
        it -> {
          stringBuffer.append(it.toString());
        });
    stringBuffer.append(
        "---------------------------------------------------------------------------------------------------------------------------------------\n");
    return stringBuffer.toString();
  }
}
