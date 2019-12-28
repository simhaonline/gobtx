package com.gobtx.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Aaron Kuai on 2019/11/7.
 */

public interface ErrorCode {
    Logger logger = LoggerFactory.getLogger(ErrorCode.class);

    int code();

    String message();

    int getPrefix();

    default String debug() {
        return "{code:" + getPrefix() + code() + ",message:" + message() + "}";
    }

    static <T extends Enum<T> & ErrorCode> T safeValueOf(Class<T> clazz, String name) {

        try {
            return Enum.valueOf(clazz, name);
        } catch (Throwable e) {
            logger.error("FAIL_MAPPER_{}_ERROR_CODE {},{}", clazz.getSimpleName().toUpperCase(), name, e);
            return (T) NA.NA;
        }
    }

    enum NA implements ErrorCode {
        NA;

        @Override
        public int code() {
            return getPrefix();
        }

        @Override
        public String message() {
            return "Unkonw Exception";
        }

        @Override
        public int getPrefix() {
            return 9_00_000;
        }
    }

    class ErrorCode500 implements ErrorCode {
        private String message;

        public ErrorCode500(String message) {
            this.message = message;
        }

        public ErrorCode500() {
        }

        public ErrorCode500 setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public int code() {
            return 500;
        }

        @Override
        public String message() {
            return message;
        }

        @Override
        public int getPrefix() {
            return 0;
        }
    }


    static String swaggerResponse(ErrorCode... errorCodes) {
        if (errorCodes == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\t@ApiResponses({\n");
        String sp = "";
        for (ErrorCode errorCode : errorCodes) {
            builder.append(sp).append("\t\t@ApiResponse(code = ").append(errorCode.code()).append(",message=\"").append(errorCode.message()).append("\")");
            sp = ",\n";
        }
        builder.append("\n\t})");
        return builder.toString();
    }
}
