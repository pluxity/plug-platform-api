package com.pluxity.global.constant;

import static org.springframework.http.HttpStatus.*;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum SuccessCode implements Code {
    SUCCESS(OK, "성공"),
    SUCCESS_CREATE(CREATED, "등록 성공"),
    SUCCESS_PUT(ACCEPTED, "수정 성공"),
    SUCCESS_PATCH(ACCEPTED, "수정 성공"),
    SUCCESS_DELETE(NO_CONTENT, "삭제 성공"),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getStatusName() {
        return this.httpStatus.name();
    }

    @Override
    public Integer getStatusValue() {
        return this.httpStatus.value();
    }
}
