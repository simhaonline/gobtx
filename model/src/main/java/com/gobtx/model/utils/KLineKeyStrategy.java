package com.gobtx.model.utils;

import java.util.Calendar;

/** Created by Aaron Kuai on 2019/11/12. */
@FunctionalInterface
public interface KLineKeyStrategy {

  long timeKey(final Calendar calendar, final long time);
}
