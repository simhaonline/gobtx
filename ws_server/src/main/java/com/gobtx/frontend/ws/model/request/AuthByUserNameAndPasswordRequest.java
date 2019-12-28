package com.gobtx.frontend.ws.model.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
public class AuthByUserNameAndPasswordRequest implements Serializable {

    @NotEmpty(message = "USER_NAME_IS_NULL")
    private String username;

    @NotEmpty(message = "PASSWORD_IS_NULL")
    @Length(min = 6,message = "PASSWORD_LENGTH_LG6")
    private String password;


    public String getUsername() {
        return username;
    }

    public AuthByUserNameAndPasswordRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthByUserNameAndPasswordRequest setPassword(String password) {
        this.password = password;
        return this;
    }
}
