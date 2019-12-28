package com.gobtx.frontend.ws.service;

import com.gobtx.model.persist.Customer;

import java.util.Optional;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
public interface CustomerService {

    Optional<Customer> findByUsername(final String username);

    Optional<Customer> findByCustomerId(final long customerId);

    Optional<Customer> findByPhoneNumber(final String phoneNumber);

    Optional<Customer> findByEmailAddress(final String emailAddress);

    boolean existsByPhoneNumber(final String phoneNumber);
}
