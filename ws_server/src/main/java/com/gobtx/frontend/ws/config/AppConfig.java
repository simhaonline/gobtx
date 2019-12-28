package com.gobtx.frontend.ws.config;

import com.gobtx.common.web.utils.JwtTokenHelper;
import com.gobtx.frontend.ws.news.NewsBishijieSpider;
import com.gobtx.frontend.ws.news.NewsSpider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.reactive.config.EnableWebFlux;

/** Created by Aaron Kuai on 2019/11/8. */
@Configuration
@EnableWebFlux
public class AppConfig {

  @Bean
  public JwtTokenHelper jwtTokenHelper() {
    return new JwtTokenHelper();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean(name = "globalGson")
  public Gson globalGson() {
    return new GsonBuilder()
        // .serializeNulls()
        .create();
  }

  @Bean
  public NewsSpider newsSpider() {
    return new NewsBishijieSpider();
  }
}
