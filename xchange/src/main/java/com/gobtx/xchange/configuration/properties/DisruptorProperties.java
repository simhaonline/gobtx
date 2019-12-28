package com.gobtx.xchange.configuration.properties;

import com.gobtx.common.CPUModel;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Created by Aaron Kuai on 2019/11/13. */
@Component
@ConfigurationProperties(prefix = "disruptor")
public class DisruptorProperties {

  protected int bufferSize = 2 << 13;

  protected ProducerType producerType = ProducerType.MULTI;

  // protected WaitStrategy waitStrategy;

  protected CPUModel cpuModel = CPUModel.DEFAULT;

  // Whether to trigger the disruptor flush?
  protected boolean disruptorFlush;

  // if leverage the disruptor to trigger the flush this is the delay
  // the out engine will trigger event at regular speed
  protected int delayInSeconds = 10;

  public int getBufferSize() {
    return bufferSize;
  }

  public DisruptorProperties setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    return this;
  }

  public ProducerType getProducerType() {
    return producerType;
  }

  public DisruptorProperties setProducerType(ProducerType producerType) {
    this.producerType = producerType;
    return this;
  }

  public CPUModel getCpuModel() {
    return cpuModel;
  }

  public DisruptorProperties setCpuModel(CPUModel cpuModel) {
    this.cpuModel = cpuModel;
    return this;
  }

  public boolean isDisruptorFlush() {
    return disruptorFlush;
  }

  public DisruptorProperties setDisruptorFlush(boolean disruptorFlush) {
    this.disruptorFlush = disruptorFlush;
    return this;
  }

  public int getDelayInSeconds() {
    return delayInSeconds;
  }

  public DisruptorProperties setDelayInSeconds(int delayInSeconds) {
    this.delayInSeconds = delayInSeconds;
    return this;
  }
}
