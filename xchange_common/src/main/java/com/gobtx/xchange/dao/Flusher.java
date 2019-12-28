package com.gobtx.xchange.dao;

import com.gobtx.model.domain.OHLCData;
import com.gobtx.model.domain.OHLCKeyData;

import java.util.Collection;

/** Created by Aaron Kuai on 2019/11/14. */
public interface Flusher {

  void batch(final Collection<OHLCData> values);

  void save(final OHLCData value);

  OHLCData get(final OHLCKeyData key);
}
