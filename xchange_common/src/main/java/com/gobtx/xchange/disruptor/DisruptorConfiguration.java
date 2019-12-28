package com.gobtx.xchange.disruptor;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

/** Created by Aaron Kuai on 2019/11/11. */
public class DisruptorConfiguration {

  protected int bufferSize;

  protected ProducerType producerType;

  protected WaitStrategy waitStrategy;

  // How Parallel the handle of flush DB
  protected int flushParallelCnt;

  protected int processParallelCnt;

  // Whether to trigger the disruptor flush?
  protected boolean disruptorFlush;

  // if leverage the disruptor to trigger the flush this is the delay
  // the out engine will trigger event at regular speed
  protected int delayInSeconds;

  public int getBufferSize() {
    return bufferSize;
  }

  public DisruptorConfiguration setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    return this;
  }

  public ProducerType getProducerType() {
    return producerType;
  }

  public DisruptorConfiguration setProducerType(ProducerType producerType) {
    this.producerType = producerType;
    return this;
  }

  public WaitStrategy getWaitStrategy() {
    return waitStrategy;
  }

  public DisruptorConfiguration setWaitStrategy(WaitStrategy waitStrategy) {
    this.waitStrategy = waitStrategy;
    return this;
  }

  public int getFlushParallelCnt() {
    return flushParallelCnt;
  }

  public DisruptorConfiguration setFlushParallelCnt(int flushParallelCnt) {
    this.flushParallelCnt = flushParallelCnt;
    return this;
  }

  public int getProcessParallelCnt() {
    return processParallelCnt;
  }

  public DisruptorConfiguration setProcessParallelCnt(int processParallelCnt) {
    this.processParallelCnt = processParallelCnt;
    return this;
  }

  public boolean isDisruptorFlush() {
    return disruptorFlush;
  }

  public DisruptorConfiguration setDisruptorFlush(boolean disruptorFlush) {
    this.disruptorFlush = disruptorFlush;
    return this;
  }

  public int getDelayInSeconds() {
    return delayInSeconds;
  }

  public DisruptorConfiguration setDelayInSeconds(int delayInSeconds) {
    this.delayInSeconds = delayInSeconds;
    return this;
  }
}
