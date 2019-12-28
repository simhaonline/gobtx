package com.gobtx.common.mixin;

public interface AccountIdAware {
    default String getAccountId(){
        return "";
    }
}
