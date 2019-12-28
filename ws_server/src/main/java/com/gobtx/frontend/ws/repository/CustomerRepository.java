package com.gobtx.frontend.ws.repository;

import com.gobtx.model.persist.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByUsername(final String username);

    Optional<Customer> findByCustomerId(final long customerId);

    Optional<Customer> findByPhoneNumber(final String phoneNumber);

    Optional<Customer> findByEmailAddress(final String emailAddress);

    @Query(value = "select count(0) from customer where phoneNumber = ?1 and status in (0,1)", nativeQuery = true)
    int existsByPhoneNumber(final String phoneNumber);

}
