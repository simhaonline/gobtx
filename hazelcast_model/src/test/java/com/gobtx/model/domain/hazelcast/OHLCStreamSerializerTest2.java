package com.gobtx.model.domain.hazelcast;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCDataImpl;
import com.gobtx.model.domain.OHLCKeyData;
import com.gobtx.model.domain.OHLCKeyDataImpl;
import com.hazelcast.config.Config;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/12. */
public class OHLCStreamSerializerTest2 {

  protected static HazelcastInstance instance;
  protected static MemorySize memorySize;

  static {
    System.setProperty("hazelcast.version.check.enabled", "false");
    System.setProperty("hazelcast.socket.bind.any", "false");
    System.setProperty("java.net.preferIPv4Stack", "true");
    System.setProperty("hazelcast.multicast.group", "224.33.55.79");
    // System.setProperty("hazelcast.logging.type", "log4j2");
  }

  @BeforeClass
  public static void init() {

    memorySize = MemorySize.parse("1G", MemoryUnit.GIGABYTES);

    InputStream configInputStream =
        OHLCStreamSerializerTest2.class.getResourceAsStream("/hazelcast-hd-memory.xml");
    Config config = new XmlConfigBuilder(configInputStream).build();
    config.setLicenseKey("FOR_EVER_YOUNG");

    NativeMemoryConfig memoryConfig = config.getNativeMemoryConfig();
    if (!memoryConfig.isEnabled()) {
      memoryConfig.setSize(memorySize).setEnabled(true);
      memoryConfig.setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.POOLED);
    }

    instance = Hazelcast.newHazelcastInstance(config);
  }

  @Test
  public void write() {

    final IMap<OHLCKeyData, OHLCData> ohlc = instance.getMap("test");

    final OHLCKeyData key = OHLCKeyDataImpl.build("btcusdt", 12332);

    ohlc.set(
        key,
        new OHLCDataImpl()
            .setSymbol("btcusdt")
            .setTimeKey(12332)
            .setOpenTime(111111111l)
            .setCloseTime(999999999l)
            .setOpen(BigDecimal.valueOf(3333333333l))
            .setHigh(BigDecimal.valueOf(4444444444l))
            .setLow(BigDecimal.valueOf(5555555555l))
            .setClose(BigDecimal.valueOf(6666666666l))
            .setAmount(BigDecimal.valueOf(777777777l))
            .setVolume(BigDecimal.valueOf(8888888888l))
            .setNumberOfTrades(9999999999l));

    OHLCData res = ohlc.get(key);

    System.out.println(res);

    Assert.assertEquals(res.getOpen(), BigDecimal.valueOf(3333333333l));
  }
}
