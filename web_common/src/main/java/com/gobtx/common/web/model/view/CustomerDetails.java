package com.gobtx.common.web.model.view;

import com.gobtx.common.mixin.AccountIdAndCustomerIdAndUserNameAware;
import com.gobtx.common.mixin.PhoneNumberAware;
import com.gobtx.model.persist.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
public class CustomerDetails implements
        UserDetails,
        AccountIdAndCustomerIdAndUserNameAware,
        PhoneNumberAware {

    protected final Customer customer;

    public CustomerDetails(final Customer customer) {
        this.customer = customer;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getPassword() {
        return customer.getCryptoPassword();
    }

    @Override
    public String getUsername() {
        return customer.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getAccountId() {
        return customer.getDefaultAccountId();
    }

    @Override
    public long getCustomerId() {
        return customer.getCustomerId();
    }

    @Override
    public String getPhoneNumber() {
        return customer.getPhoneNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerDetails that = (CustomerDetails) o;
        return Objects.equals(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer);
    }
}
