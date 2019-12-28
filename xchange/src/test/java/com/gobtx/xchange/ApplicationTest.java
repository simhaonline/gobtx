package com.gobtx.xchange;

import com.gobtx.common.Env;
import com.gobtx.xchange.configuration.AppConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Created by Aaron Kuai on 2019/11/13. */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AppConfiguration.class)
public class ApplicationTest {

  static {
    System.setProperty("app.env", "dev");
    System.setProperty("instance.name", "Xchange");
    Env.init();
  }

  @Autowired ApplicationContext context;

  static final Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

  @Test
  public void testStart() {

    Assert.assertNotNull(context);
  }
}
