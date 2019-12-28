package com.gobtx.common.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public enum GlobalScheduleService {
  INSTANCE;

  public final ScheduledExecutorService executorService;

  GlobalScheduleService() {

    int sThreadCnt = Integer.min(2, Runtime.getRuntime().availableProcessors() / 3);

    sThreadCnt = Integer.max(sThreadCnt, 4);

    executorService =
        Executors.newScheduledThreadPool(
            sThreadCnt,
            new ThreadFactory() {
              final AtomicLong cnt = new AtomicLong(0L);
              final ThreadFactory backendFactory = Executors.defaultThreadFactory();

              @Override
              public Thread newThread(Runnable runnable) {

                final Thread thread = backendFactory.newThread(runnable);
                thread.setName("GLOBAL-SCHEDULE-" + cnt.getAndIncrement());
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

  /**
   * Creates and executes a one-shot action that becomes enabled after the given delay.
   *
   * @param command the task to execute
   * @param delay the time from now to delay execution
   * @param unit the time unit of the delay parameter
   * @return a ScheduledFuture representing pending completion of the task and whose {@code get()}
   *     method will return {@code null} upon completion
   * @throws RejectedExecutionException if the task cannot be scheduled for execution
   * @throws NullPointerException if command is null
   */
  public ScheduledFuture<?> schedule(
      final Runnable command, final long delay, final TimeUnit unit) {

    return executorService.schedule(command, delay, unit);
  }

  /**
   * Creates and executes a ScheduledFuture that becomes enabled after the given delay.
   *
   * @param callable the function to execute
   * @param delay the time from now to delay execution
   * @param unit the time unit of the delay parameter
   * @param <V> the type of the callable's result
   * @return a ScheduledFuture that can be used to extract result or cancel
   * @throws RejectedExecutionException if the task cannot be scheduled for execution
   * @throws NullPointerException if callable is null
   */
  public <V> ScheduledFuture<V> schedule(
      final Callable<V> callable, final long delay, final TimeUnit unit) {

    return executorService.schedule(callable, delay, unit);
  }

  /**
   * Creates and executes a periodic action that becomes enabled first after the given initial
   * delay, and subsequently with the given period; that is executions will commence after {@code
   * initialDelay} then {@code initialDelay+period}, then {@code initialDelay + 2 * period}, and so
   * on. If any execution of the task encounters an exception, subsequent executions are suppressed.
   * Otherwise, the task will only terminate via cancellation or termination of the executor. If any
   * execution of this task takes longer than its period, then subsequent executions may start late,
   * but will not concurrently execute.
   *
   * @param command the task to execute
   * @param initialDelay the time to delay first execution
   * @param period the period between successive executions
   * @param unit the time unit of the initialDelay and period parameters
   * @return a ScheduledFuture representing pending completion of the task, and whose {@code get()}
   *     method will throw an exception upon cancellation
   * @throws RejectedExecutionException if the task cannot be scheduled for execution
   * @throws NullPointerException if command is null
   * @throws IllegalArgumentException if period less than or equal to zero
   */
  public ScheduledFuture<?> scheduleAtFixedRate(
      final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {

    return executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  /**
   * Creates and executes a periodic action that becomes enabled first after the given initial
   * delay, and subsequently with the given delay between the termination of one execution and the
   * commencement of the next. If any execution of the task encounters an exception, subsequent
   * executions are suppressed. Otherwise, the task will only terminate via cancellation or
   * termination of the executor.
   *
   * @param command the task to execute
   * @param initialDelay the time to delay first execution
   * @param delay the delay between the termination of one execution and the commencement of the
   *     next
   * @param unit the time unit of the initialDelay and delay parameters
   * @return a ScheduledFuture representing pending completion of the task, and whose {@code get()}
   *     method will throw an exception upon cancellation
   * @throws RejectedExecutionException if the task cannot be scheduled for execution
   * @throws NullPointerException if command is null
   * @throws IllegalArgumentException if delay less than or equal to zero
   */
  public ScheduledFuture<?> scheduleWithFixedDelay(
      final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {

    return executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
  }
}
