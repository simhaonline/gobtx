package com.gobtx.frontend.ws.service;

import com.gobtx.frontend.ws.repository.CustomerRepository;
import com.gobtx.model.persist.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@Service
public class CustomerServiceImpl implements CustomerService {


    @Autowired
    CustomerRepository customerRepository;


    @Override
    public Optional<Customer> findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    @Override
    public Optional<Customer> findByCustomerId(long customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    @Override
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public Optional<Customer> findByEmailAddress(String emailAddress) {
        return customerRepository.findByEmailAddress(emailAddress);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return customerRepository.existsByPhoneNumber(phoneNumber) > 0;
    }
}
