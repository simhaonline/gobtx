package com.gobtx.frontend.ws.push;

import com.gobtx.common.CPUModel;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Created by Aaron Kuai on 2019/11/20. */
@Component
@ConfigurationProperties(prefix = "disruptor")
public class DisruptorProperties {

  protected int bufferSize = 2 << 13;

  protected ProducerType producerType = ProducerType.MULTI;

  // protected WaitStrategy waitStrategy;

  protected CPUModel cpuModel = CPUModel.DEFAULT;

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
}
