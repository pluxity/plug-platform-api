package com.pluxity.global.constant;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements Code {
    DUPLICATE_USERNAME(BAD_REQUEST, "이미 존재 하는 아이디 입니다."),

    INVALID_ID_OR_PASSWORD(BAD_REQUEST, "아이디 또는 비밀번호가 틀렸습니다."),

    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "ACCESS 토큰이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "REFRESH 토큰이 유효하지 않습니다."),
    INVALID_TOKEN_FORMAT(UNAUTHORIZED, "유효하지 않은 토큰 형식입니다."),

    EXPIRED_ACCESS_TOKEN(UNAUTHORIZED, "ACCESS 토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "REFRESH 토큰이 만료되었습니다."),

    NOT_FOUND_USER(BAD_REQUEST, "해당 회원이 존재하지 않습니다."),

    NOT_AUTHORIZED(UNAUTHORIZED, "권한이 없습니다.");

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