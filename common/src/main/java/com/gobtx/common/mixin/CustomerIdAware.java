package com.gobtx.common.mixin;

public interface CustomerIdAware {
    default long getCustomerId(){
        return -1L;
    }
}
