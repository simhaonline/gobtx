package com.gobtx.common.mixin;

public interface UsernameAware {
    default String getUsername(){
        return "";
    }
}
