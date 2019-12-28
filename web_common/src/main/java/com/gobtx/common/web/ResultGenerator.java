package com.gobtx.common.web;


import com.gobtx.common.ErrorCode;

/**
 * Generator the result
 */
public class ResultGenerator {

    public static Result success() {
        return new Result()
                .setCode(HttpResponseStatus.OK.code());
    }

    public static <T> Result<T> success(T data) {
        return new Result()
                .setCode(HttpResponseStatus.OK.code())
                .setData(data);
    }

    public static <T> Result<T> success(T data,
                                        String message) {
        return new Result()
                .setCode(HttpResponseStatus.OK.code())
                .setMessage(message)
                .setData(data);
    }

    public static Result fail(String message) {
        return new Result()
                .setCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .setMessage(message);
    }


    public static <T> Result<T> fail(T data,
                                     String message) {
        return new Result()
                .setCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .setData(data)
                .setMessage(message);
    }


    public static Result fail(String message, final HttpResponseStatus status) {
        return new Result()
                .setCode(status)
                .setMessage(message);
    }

    public static Result fail(ErrorCode code) {
        return new Result()
                .setCode(HttpResponseStatus.OK.code())
                .setBizCode(code.code())
                .setMessage(code.message());
    }


    public static <T> Result<T> fail(final T data,
                                     final String message,
                                     final HttpResponseStatus status) {
        return new Result()
                .setCode(status)
                .setData(data)
                .setMessage(message);
    }

    public static <T> Result<T> fail(final T data,
                                     final String message,
                                     final int bizCode) {
        return new Result()
                .setCode(HttpResponseStatus.OK.code())
                .setBizCode(bizCode)
                .setMessage(message)
                .setData(data);
    }


    public static <T> Result<T> forbidden(final String message) {
        return new Result()
                .setCode(HttpResponseStatus.FORBIDDEN)
                .setMessage(message);
    }


    public static <T> Result<T> unauthorized(String message) {
        return new Result()
                .setCode(HttpResponseStatus.UNAUTHORIZED)
                .setMessage(message);
    }

    public static <T> Result<T> unauthorized() {
        return new Result()
                .setCode(HttpResponseStatus.UNAUTHORIZED);
    }



    public static <T> Result<T> fail404(T data) {
        return new Result<>()
                .setCode(404)
                .setData(data);
    }

}
