package com.gobtx.frontend.ws;

import com.gobtx.common.Env;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(Application.class)
public class ApplicationTest {


    static {
        System.setProperty("app.env", "dev");
        System.setProperty("instance.name", "websocket test");
        Env.init();
    }



    @Autowired
    ApplicationContext context;


    static final Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

    @Test
    public void isDone() {

        Assert.assertTrue("Go Here? ", true);

        Assert.assertNotNull(context);

        logger.error(context.getApplicationName());
    }


//    @Deployment
//    public static JavaArchive createDeployment() {
//        return ShrinkWrap.create(JavaArchive.class)
//                .addClass(Application.class)
//                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
//    }

}
