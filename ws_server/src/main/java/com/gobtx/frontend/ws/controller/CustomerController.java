package com.gobtx.frontend.ws.controller;

import com.gobtx.common.web.Result;
import com.gobtx.common.web.ResultGenerator;
import com.gobtx.common.web.model.view.CustomerDetails;
import com.gobtx.common.web.utils.JwtTokenHelper;
import com.gobtx.frontend.ws.model.request.AuthByUserNameAndPasswordRequest;
import com.gobtx.frontend.ws.service.CustomerService;
import com.gobtx.model.persist.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    CustomerService customerService;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;


    @Autowired
    JwtTokenHelper tokenHelper;


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Mono<Result> login(@RequestBody AuthByUserNameAndPasswordRequest ar) {

        final Optional<Customer>
                optionalCustomer =
                customerService
                        .findByUsername(ar.getUsername());

        if (optionalCustomer.isPresent()) {

            final Customer customer = optionalCustomer.get();

            if (passwordEncoder.encode(ar.getPassword()).equals(customer.getCryptoPassword())) {

                return Mono
                        .just(ResultGenerator
                                .success(tokenHelper
                                        .generateToken(new CustomerDetails(customer))));

            } else {

                logger.warn("FAIL_VALIDATE_PASSWORD {}", ar.getUsername());
                return Mono.just(ResultGenerator.unauthorized());

            }

        } else {

            logger.warn("CUSTOMER_NOT_EXIST {}", ar.getUsername());
            return Mono.just(ResultGenerator.unauthorized());

        }
    }


    @RequestMapping(value = "/echo")
    public Mono<Result> echo() {
        //Echo self
        //Must be auth

        return getContext()
                .map(SecurityContext::getAuthentication)
                .map(new Function<Authentication, Result>() {

                    @Override
                    public Result apply(Authentication authentication) {

                        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                        if (userDetails != null) {
                            return ResultGenerator.success("Echo " + userDetails.getUsername());
                        }

                        return ResultGenerator.unauthorized();
                    }
                }).defaultIfEmpty(ResultGenerator.unauthorized());


    }


    @RequestMapping(value = "/hello")
    public Mono<ServerResponse> hello(final ServerRequest serverRequest) {

        return serverRequest
                .principal()
                .map(Principal::getName)
                .flatMap(username ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Collections.singletonMap("message", "Hello " + username + "!"))
                );
    }

}
