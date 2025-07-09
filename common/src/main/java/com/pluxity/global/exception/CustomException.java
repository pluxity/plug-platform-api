package com.pluxity.global.exception;

import com.pluxity.global.constant.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] params;

    public CustomException(ErrorCode errorCode, Object... params) {
        super(formatMessage(errorCode, params));
        this.errorCode = errorCode;
        this.params = params;
    }

    private static String formatMessage(ErrorCode errorCode, Object... params) {
        if (params == null || params.length == 0) {
            return errorCode.getMessage();
        }
        try {
            return String.format(errorCode.getMessage(), params);
        } catch (Exception e) {
            return errorCode.getMessage() + " (format params: " + java.util.Arrays.toString(params) + ")";
        }
    }
}
