package com.gobtx.frontend.ws;

import com.gobtx.common.Env;
import com.gobtx.common.Utils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by Aaron Kuai on 2019/11/7.
 */
@SpringBootApplication(
        scanBasePackages = "com.gobtx.frontend",
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                MultipartAutoConfiguration.class
                //CacheAutoConfiguration.class,  //load manually
                //DataSourceTransactionManagerAutoConfiguration.class,
                //JndiConnectionFactoryAutoConfiguration.class,JpaRepositoriesAutoConfiguration.class
        }
)
@EnableTransactionManagement
@PropertySources({
        @PropertySource("classpath:application/common.properties"),
        @PropertySource("classpath:application/${app.env:dev}.properties")
})
@EnableAsync
public class Application {

    public static String[] ARGS;

    public static void main(String[] args) {


        //0. Pre-process of the app.env
        if ("NA".equals(Utils.sysProperty("app.env", "NA"))) {

            System.err.println("No specific app.env set so back to the dev env please specify with -Dapp.env=dev|qa|prod");
            System.setProperty("app.env", "dev");
        }

        //web.app.type
        //SERVLET
        //REACTIVE


        //1. Pre-process of the web app type
        WebApplicationType type = WebApplicationType.SERVLET;

        final String sType = Utils.sysProperty("web.app.type", "na").toLowerCase();

        if ("na".equals(sType)) {

            //default to reactive
            System.setProperty("web.app.type", "reactive");
            type = WebApplicationType.REACTIVE;
            System.err.println("No specific web.app.type, so back to the REACTIVE please specify with -Dweb.app.type=servlet|reactive");

        } else {

            switch (sType) {
                case "servlet":
                    type = WebApplicationType.SERVLET;
                    System.setProperty("web.app.type", "servlet");
                    break;
                case "reactive":
                    type = WebApplicationType.REACTIVE;
                    System.setProperty("web.app.type", "reactive");
                    break;
            }
        }

        Env.init();
        ARGS = args;

        new SpringApplicationBuilder(Application.class)
                .web(type)
                .run(args);

//        SpringApplication.run(Application.class, args);
    }

}
