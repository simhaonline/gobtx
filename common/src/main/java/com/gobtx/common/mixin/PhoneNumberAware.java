package com.gobtx.common.mixin;

/**
 * Created by Aaron Kuai on 2019/11/8.
 */
public interface PhoneNumberAware {

    default String getPhoneNumber() {
        return "";
    }
}
