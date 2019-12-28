package com.gobtx.frontend.ws.config.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@Profile("reactive")
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebFluxSecurityConfig {

    static final Logger logger = LoggerFactory.getLogger(WebFluxSecurityConfig.class);

    @Value("#{'${security.white.url:/**/logon,/**/register}'.split(',')}")
    protected List<String> securityWhiteUrls;

    @Autowired
    protected WebFluxAuthenticationManager authenticationManager;

    @Autowired
    protected WebFluxSecurityContextRepository securityContextRepository;


    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {

        logger.warn(
                "\n\n=============SECURITY_WHITE_URL=============\n\t{}\n\n===============================\n",
                String.join("\n\t", securityWhiteUrls));

        return http
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) -> {
                    return Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    });
                }).accessDeniedHandler((swe, e) -> {
                    return Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    });
                })
                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers(
                        HttpMethod.GET,
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/swagger-resources/**",
                        "/v2/api-docs/**"
                )
                .permitAll()
                .pathMatchers(
                        securityWhiteUrls.toArray(new String[0])
                ).permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                //.pathMatchers("/login").permitAll()
                .anyExchange().authenticated()
                .and()

                .build();
    }

}
