package com.gobtx.xchange.dao;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCKeyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/** Created by Aaron Kuai on 2019/11/14. */
public class MockFlusher implements Flusher {

  static final Logger logger = LoggerFactory.getLogger(MockFlusher.class);

  @Override
  public void batch(Collection<OHLCData> values) {
    logger.warn("MOCK_BATCH_SAVE {}", values.size());
  }

  @Override
  public void save(OHLCData value) {
    logger.warn("MOCK_NO_SAVE {}", value);
  }

  @Override
  public OHLCData get(OHLCKeyData key) {
    logger.warn("MOCK_NO_LOADER {}", key);
    return null;
  }
}
