package com.hazelcast.license.util;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.memory.DefaultMemoryStats;
import com.hazelcast.memory.HazelcastMemoryManager;
import com.hazelcast.memory.MemoryStats;
import com.hazelcast.nio.serialization.EnterpriseSerializationService;

import java.lang.reflect.Field;

public class MemoryStatsUtil {

  private static final Field ORIGINAL_FIELD;

  static {
    try {
      ORIGINAL_FIELD = HazelcastInstanceProxy.class.getDeclaredField("original");
      ORIGINAL_FIELD.setAccessible(true);
    } catch (Throwable t) {
      throw new IllegalStateException(
          "Unable to get `original` field in `HazelcastInstanceProxy`!", t);
    }
  }

  private static HazelcastInstanceImpl getHazelcastInstanceImpl(HazelcastInstance hz) {
    HazelcastInstanceImpl impl = null;
    if (hz instanceof HazelcastInstanceProxy) {
      try {
        impl = (HazelcastInstanceImpl) ORIGINAL_FIELD.get(hz);
      } catch (Throwable t) {
        throw new IllegalStateException(
            "Unable to get value of `original` in `HazelcastInstanceProxy`!", t);
      }
    } else if (hz instanceof HazelcastInstanceImpl) {
      impl = (HazelcastInstanceImpl) hz;
    }
    return impl;
  }

  public static Node getNode(HazelcastInstance hz) {
    HazelcastInstanceImpl impl = getHazelcastInstanceImpl(hz);
    return impl != null ? impl.node : null;
  }

  static MemoryStats getMemoryStats(HazelcastInstance hz) {
    // use this method or another way for getting "Node" from a "HazelcastInstance"
    Node node = getNode(hz);
    if (node != null) {
      EnterpriseSerializationService serializationService =
          (EnterpriseSerializationService) node.getSerializationService();
      HazelcastMemoryManager memoryManager = serializationService.getMemoryManager();
      return memoryManager.getMemoryStats();
    } else {
      return new DefaultMemoryStats();
    }
  }
}
