package com.gobtx.common.web;


import java.util.function.Function;

/**
 * API result wrapper
 */
public class Result<T> {
    private int code;
    private int bizCode;
    private String message;
    private T data;


    public int getBizCode() {
        return bizCode;
    }

    public Result<T> setBizCode(int bizCode) {
        this.bizCode = bizCode;
        return this;
    }

    public Result setCode(int resultCode) {
        this.code = resultCode;
        return this;
    }

    public Result setCode(HttpResponseStatus httpResponseStatus) {
        this.code = httpResponseStatus.code();
        return this;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result setData(T data) {
        this.data = data;
        return this;
    }

    public <N> Result<N> newborn() {
        return new Result<>()
                .setMessage(getMessage())
                .setBizCode(getBizCode())
                .setCode(getCode());
    }

    public <N> Result<N> newborn(Function<T, N> function) {
        return newborn().setData(function.apply(getData()));
    }


}
