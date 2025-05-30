package com.pluxity.global.exception;

import com.pluxity.global.constant.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private String codeName;
    private ErrorCode errorCode;
    private HttpStatus httpStatus;
    private final String message;

    public CustomException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.codeName = errorCode.getStatusName();
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }

    public CustomException(String codeName, HttpStatus httpStatus, String message) {
        this.codeName = codeName;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public CustomException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
