package com.gobtx.frontend.ws.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Created by Aaron Kuai on 2019/11/7.
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @GetMapping("time")
    public Mono<Long> getTime(final ServerWebExchange exchange) {

        final String ips = exchange.getRequest().getRemoteAddress().toString();

        if (logger.isDebugEnabled()) {
            logger.debug("GET_TIME_FROM_IP {}", ips);
        }

        ReactiveSecurityContextHolder
                .getContext()
                .map(context ->
                        context.getAuthentication());

        return Mono.just(System.currentTimeMillis());
    }

}
