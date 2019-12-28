package com.gobtx.xchange;

import com.gobtx.common.Env;
import com.gobtx.common.Utils;
import com.gobtx.xchange.configuration.AppConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** Created by Aaron Kuai on 2019/11/13. */
public class Application {

  public static String[] ARGS;

  public static void main(String[] args) {

    // 0. Pre-process of the app.env
    if ("NA".equals(Utils.sysProperty("app.env", "NA"))) {

      System.err.println(
          "No specific app.env set so back to the dev env please specify with -Dapp.env=dev|qa|prod");
      System.setProperty("app.env", "dev");
    }

    Env.init();
    ARGS = args;

    new SpringApplicationBuilder(AppConfiguration.class).web(WebApplicationType.NONE).run(args);
  }
}
