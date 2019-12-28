package com.gobtx.frontend.ws.config.reactive;

import com.gobtx.common.web.model.view.CustomerDetails;
import com.gobtx.common.web.utils.JwtTokenHelper;
import com.gobtx.frontend.ws.service.CustomerService;
import com.gobtx.model.persist.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;


/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@Component
@Profile("reactive")
public class WebFluxAuthenticationManager implements ReactiveAuthenticationManager {

    static final Logger logger = LoggerFactory.getLogger(WebFluxAuthenticationManager.class);

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    CustomerService customerService;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Authentication> authenticate(Authentication authentication) {

        if (logger.isDebugEnabled()) {
            logger.debug("START_AUTH {}", authentication.getPrincipal());
        }

        final String authToken = authentication.getCredentials().toString();
        final JwtTokenHelper.JWTResult jwtResult = jwtTokenHelper.parse(authToken, secret);

        if (jwtResult.isValid()) {

            final String username = jwtTokenHelper.getUsername(jwtResult);
            Optional<Customer> optionalCustomer = customerService.findByUsername(username);

            if (optionalCustomer.isPresent()) {

                Customer customer = optionalCustomer.get();

                final CustomerDetails details = new CustomerDetails(customer);

                final UsernamePasswordAuthenticationToken
                        auth =
                        new UsernamePasswordAuthenticationToken(
                                details,
                                null,
                                details.getAuthorities());

                //all the frontend customer have no right etc
                return Mono.just(auth);

            } else {
                logger.warn("USER_NAME_NOT_EXIST_JWT {}", username);
                return Mono.empty();
            }
        } else {
            logger.warn("INVALIDATE_JWT");
            return Mono.empty();

        }
    }
}
