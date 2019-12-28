package com.gobtx.frontend.ws.config.reactive;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/** Created by Aaron Kuai on 2019/12/2. */
@Profile("reactive")
@Configuration
public class CorsGlobalConfiguration implements WebFluxConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {
    corsRegistry
        .addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "HEAD", "DELETE", "OPTION")
        .maxAge(3600);
  }
}
