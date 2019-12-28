package com.gobtx.frontend.ws.config;

import com.gobtx.common.Env;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.BlockingQueue;

/** Created by Aaron Kuai on 2019/12/21. */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HazelcastConfiguration.class)
public class HazelcastConfigurationTest {
  static {
    System.setProperty("app.env", "dev");
    System.setProperty("instance.name", "XchangeClient");
    Env.init();
  }

  @Autowired HazelcastInstance instance;

  @Test
  public void initHazelcast() throws InterruptedException {

    BlockingQueue<String> queue = instance.getQueue("queue");
    queue.put("Hello!");
    System.out.println("Message sent by Hazelcast Client!");

    HazelcastClient.shutdownAll();
  }
}
