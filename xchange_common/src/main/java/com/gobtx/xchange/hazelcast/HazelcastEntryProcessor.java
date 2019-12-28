package com.gobtx.xchange.hazelcast;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.OHLCKeyData;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

/** Created by Aaron Kuai on 2019/11/12. */
@SuppressWarnings("Duplicates")
public class HazelcastEntryProcessor implements EntryProcessor<OHLCKeyData, OHLCData> {

  static final Logger logger = LoggerFactory.getLogger(HazelcastEntryProcessor.class);

  private static final long serialVersionUID = 8184070495669081133L;
  protected final long openTime;
  protected final long closeTime;
  protected final BigDecimal open;
  protected final BigDecimal high;
  protected final BigDecimal low;
  protected final BigDecimal close;

  // Previous is the normally the key thing
  protected final BigDecimal volume;
  protected final BigDecimal amount;
  protected final long numberOfTrades;

  public HazelcastEntryProcessor(
      final long openTime,
      final long closeTime,
      final BigDecimal open,
      final BigDecimal high,
      final BigDecimal low,
      final BigDecimal close,
      final BigDecimal volume,
      final BigDecimal amount,
      final long numberOfTrades) {

    this.openTime = openTime;
    this.closeTime = closeTime;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
    this.amount = amount;
    this.numberOfTrades = numberOfTrades;
  }

  @Override
  public OHLCData process(Map.Entry<OHLCKeyData, OHLCData> entry) {

    //    if (logger.isDebugEnabled()) {
    //      logger.debug(
    //          "BEFORE_SAVE {},{},{},{},{},{},{},{},{},{}",
    //          entry.getKey().getSymbol(),
    //          entry.getKey().getTimeKey(),
    //          openTime,
    //          closeTime,
    //          open,
    //          high,
    //          low,
    //          close,
    //          volume,
    //          amount);
    //    }

    if (entry.getValue() == null) {
      OHLCData data =
          new OHLCDataImpl()
              .setSymbol(entry.getKey().getSymbol())
              .setTimeKey(entry.getKey().getTimeKey())
              .setOpenTime(openTime)
              .setCloseTime(closeTime)
              .setOpen(open)
              .setHigh(high)
              .setLow(low)
              .setClose(close)
              .setVolume(volume)
              .setAmount(amount)
              .setNumberOfTrades(numberOfTrades);

      entry.setValue(data);
      return data;

    } else {

      OHLCData ohlcData = entry.getValue();

      ohlcData.setCloseTime(closeTime);

      if (low.compareTo(ohlcData.getLow()) < 0) {
        // Lower
        ohlcData.setLow(low);
      }

      if (high.compareTo(ohlcData.getHigh()) > 0) {
        // Higher
        ohlcData.setHigh(high);
      }
      ohlcData.setClose(close);

      ohlcData
          .setVolume(ohlcData.getVolume().add(volume))
          .setAmount(ohlcData.getAmount().add(amount))
          .setNumberOfTrades(ohlcData.getNumberOfTrades() + numberOfTrades);

      entry.setValue(ohlcData);
      return ohlcData;
    }
  }

  @Override
  public EntryBackupProcessor<OHLCKeyData, OHLCData> getBackupProcessor() {
    return null;
  }
}
