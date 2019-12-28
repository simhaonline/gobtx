package com.gobtx.frontend.ws.model.response;

import com.gobtx.model.persist.Customer;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
public class AuthResponse {

    private String phoneNumber;

    private String username;

    private String jwt;

    private final Customer customer;

    public AuthResponse(Customer customer) {
        this.customer = customer;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public AuthResponse setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AuthResponse setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getJwt() {
        return jwt;
    }

    public AuthResponse setJwt(String jwt) {
        this.jwt = jwt;
        return this;
    }

    public Long getId() {
        return customer.getId();
    }

    public Long getCustomerId() {
        return customer.getCustomerId();
    }

    public String getNickname() {
        return customer.getNickname();
    }

    public String getAccount() {
        return customer.getDefaultAccountId();
    }

    public Long getCreateTimestamp() {
        return customer.getCreateTimestamp();
    }

    public Long getUpdateTimestamp() {
        return customer.getUpdateTimestamp();
    }

}
