package com.gobtx.xchange.configuration;

import com.gobtx.common.Env;
import com.hazelcast.core.HazelcastInstance;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.BlockingQueue;

/** Created by Aaron Kuai on 2019/12/21. */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HazelcastRepositoryConfiguration.class)
public class HazelcastRepositoryConfigurationTest {

  static {
    // LOCAL_ADDRESS
    System.setProperty("LOCAL_ADDRESS", "127.0.0.1");
    System.setProperty("app.env", "dev");
    System.setProperty("instance.name", "Xchange");
    Env.init();
  }

  @Autowired HazelcastInstance instance;

  @Test
  public void createDeployment() {

    Assert.assertNotNull(instance);

    BlockingQueue<String> queue = instance.getQueue("queue");
    try {
      for (; ; ) {
        System.out.println(queue.take());
      }
    } catch (Throwable e) {
      System.err.println("Unable to take from the queue. Hazelcast Member is probably going down!");
    }
  }
}
