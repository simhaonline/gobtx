package com.gobtx.common.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public enum GlobalExecutorService {
  INSTANCE;

  public final ExecutorService executorService;

  GlobalExecutorService() {

    int sThreadCnt = Integer.min(2, Runtime.getRuntime().availableProcessors() / 2);
    sThreadCnt = Integer.max(sThreadCnt, 8);

    executorService =
        Executors.newFixedThreadPool(
            sThreadCnt,
            new ThreadFactory() {
              final AtomicLong cnt = new AtomicLong(0L);
              final ThreadFactory backendFactory = Executors.defaultThreadFactory();

              @Override
              public Thread newThread(Runnable runnable) {
                Thread thread = backendFactory.newThread(runnable);
                thread.setName("GLOBAL-EXECUTOR-" + cnt.getAndIncrement());
                thread.setUncaughtExceptionHandler((t, e) -> System.err.println(e.getMessage()));
                return thread;
              }
            });
  }

  public void shutdown() {
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  public <T> Future<T> submit(final Callable<T> task) {
    return executorService.submit(task);
  }

  public <T> Future<T> submit(final Runnable task, final T result) {
    return executorService.submit(task, result);
  }

  public Future<?> submit(final Runnable task) {
    return executorService.submit(task);
  }
}
