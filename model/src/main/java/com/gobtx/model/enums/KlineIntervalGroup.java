package com.gobtx.model.enums;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Created by Aaron Kuai on 2019/12/12. */
public class KlineIntervalGroup {

  public static final KlineIntervalGroup DEAD = new KlineIntervalGroup(null);

  final KlineInterval interval;

  final Set<KlineInterval> standalone = new LinkedHashSet<>();
  final Set<KlineInterval> derived = new LinkedHashSet<>();

  public KlineIntervalGroup(KlineInterval interval) {
    this.interval = interval;
  }

  public KlineInterval getInterval() {
    return interval;
  }

  public Set<KlineInterval> getStandalone() {
    return standalone;
  }

  public Set<KlineInterval> getDerived() {
    return derived;
  }

  public KlineIntervalGroup addStandalone(KlineInterval interval) {
    standalone.add(interval);
    return this;
  }

  public KlineIntervalGroup addDerived(final KlineInterval interval) {
    derived.add(interval);
    return this;
  }

  public static List<KlineIntervalGroup> group(final Set<KlineInterval> supported) {

    final List<KlineIntervalGroup> groups = new ArrayList<>();
    KlineInterval.getIntervalMap()
        .forEach(
            (interval, children) -> {
              // Some can standalone
              // Some need to derived
              final KlineIntervalGroup klineIntervalGroup = new KlineIntervalGroup(interval);
              groups.add(klineIntervalGroup);
              for (final KlineInterval ch : children) {

                if (supported.contains(ch)) {
                  klineIntervalGroup.addStandalone(ch);
                } else {
                  klineIntervalGroup.addDerived(ch);
                }
              }
            });

    return groups;
  }
}
