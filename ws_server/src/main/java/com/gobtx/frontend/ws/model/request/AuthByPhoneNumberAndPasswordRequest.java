package com.gobtx.frontend.ws.model.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
public class AuthByPhoneNumberAndPasswordRequest implements Serializable {

    @NotEmpty(message = "PHONE_NUMBER_IS_NULL")
    private String phoneNumber;

    @NotEmpty(message = "PASSWORD_IS_NULL")
    @Length(min = 6, message = "PASSWORD_LENGTH_LG6")
    private String password;


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public AuthByPhoneNumberAndPasswordRequest setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthByPhoneNumberAndPasswordRequest setPassword(String password) {
        this.password = password;
        return this;
    }
}
