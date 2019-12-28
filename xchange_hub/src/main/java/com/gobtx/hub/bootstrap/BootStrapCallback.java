package com.gobtx.hub.bootstrap;

/** Created by Aaron Kuai on 2019/11/18. */
@FunctionalInterface
public interface BootStrapCallback {

  void update(final LifecycleBootStrap bootStrap, final Status status, Object... args);
}
