package com.gobtx.frontend.ws.config.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@Component
@Profile("reactive")
public class WebFluxSecurityContextRepository implements ServerSecurityContextRepository {

    static final Logger logger = LoggerFactory.getLogger(WebFluxSecurityContextRepository.class);

    @Value("${jwt.tokenHeader:Authorization}")
    protected String tokenHeader;

    @Value("${jwt.tokenHead:Bearer}")
    protected String tokenHead;

    @Autowired
    private WebFluxAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("SecurityContextRepository#save Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {

        if (logger.isDebugEnabled()) {
            logger.debug("TRY_SECURITY {}", swe.getRequest().getPath());
        }


        final ServerHttpRequest request = swe.getRequest();

        final String authHeader = request.getHeaders().getFirst(tokenHeader);

        if (authHeader != null &&
                authHeader.startsWith(tokenHead)) {


            final String authToken = authHeader.substring(7);

            final Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);


            return authenticationManager
                    .authenticate(auth)
                    .map((authentication) -> new SecurityContextImpl(authentication));
        } else {
            return Mono.empty();
        }
    }

}
