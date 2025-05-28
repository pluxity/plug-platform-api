package com.pluxity.global.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements Code {
    DUPLICATE_USERNAME(BAD_REQUEST, "이미 존재 하는 아이디 입니다."),

    INVALID_ID_OR_PASSWORD(BAD_REQUEST, "아이디 또는 비밀번호가 틀렸습니다."),

    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "ACCESS 토큰이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "REFRESH 토큰이 유효하지 않습니다."),
    INVALID_TOKEN_FORMAT(UNAUTHORIZED, "유효하지 않은 토큰 형식입니다."),

    INVALID_FILE_TYPE(BAD_REQUEST, "적절하지 않은 파일 유형입니다."),
    INVALID_FILE_STATUS(BAD_REQUEST, "적절하지 않은 파일 상태입니다."),
    INVALID_SBM_FILE(BAD_REQUEST, "적절하지 않은 SBM 파일입니다."),

    EXPIRED_ACCESS_TOKEN(UNAUTHORIZED, "ACCESS 토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "REFRESH 토큰이 만료되었습니다."),

    FAILED_TO_ZIP_FILE(BAD_REQUEST, "파일 압축에 실패했습니다."),
    FAILED_TO_UPLOAD_FILE(INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FAILED_TO_PROCESS_SBM_FILE(BAD_REQUEST, "SBM 파일 처리에 실패했습니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다."),
    NOT_FOUND_USER(BAD_REQUEST, "해당 회원이 존재하지 않습니다."),

    NOT_AUTHORIZED(UNAUTHORIZED, "권한이 없습니다."),

    PERMISSION_DENIED(FORBIDDEN, "접근 권한이 없습니다."),

    CATEGORY_HAS_DEVICES(BAD_REQUEST, "카테고리에 등록된 디바이스가 있어 삭제할 수 없습니다."),

    EXCEED_CATEGORY_DEPTH(BAD_REQUEST, "카테고리는 깊이를 초과했습니다"),

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
