package com.gobtx.hub.bootstrap;

/** Created by Aaron Kuai on 2019/11/18. */
public interface LifecycleBootStrap {

  void start() throws InterruptedException;

  void stop();
}
