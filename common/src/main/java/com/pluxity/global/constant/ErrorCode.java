package com.pluxity.global.constant;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements Code {
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

    NOT_FOUND_USER(BAD_REQUEST, "해당 회원이 존재하지 않습니다."),

    NOT_AUTHORIZED(UNAUTHORIZED, "권한이 없습니다."),

    PERMISSION_DENIED(FORBIDDEN, "접근 권한이 없습니다."),

    CATEGORY_HAS_DEVICES(BAD_REQUEST, "카테고리에 등록된 디바이스가 있어 삭제할 수 없습니다."),

    EXCEED_CATEGORY_DEPTH(BAD_REQUEST, "카테고리는 깊이를 초과했습니다"),
    INVALID_REFERENCE(BAD_REQUEST, "요청 된 참조가 유효하지 않습니다."),

    INVALID_PARENT_CATEGORY(BAD_REQUEST, "카테고리는 자기 자신을 부모로 가질 수 없습니다."),

    ASSET_CATEGORY_HAS_ASSET(BAD_REQUEST, "에셋 카테고리에 에셋이 있어 삭제할 수 없습니다."),
    ASSET_CATEGORY_HAS_CHILDREN(BAD_REQUEST, "에셋 카테고리에 하위 카테고리가 있어 삭제할 수 없습니다."),

    DEVICE_ALREADY_HAS_FEATURE(BAD_REQUEST, "디바이스는 이미 %s Feature를 가지고 있습니다."),
    FEATURE_HAS_NOT_DEVICE(BAD_REQUEST, "%s Feature에 할당된 디바이스가 존재하지 않습니다."),
    NOT_EXIST_ASSET_CATEGORY(BAD_REQUEST, "%s 아이디 에셋에 카테고리가 존재하지 않습니다."),
    INVALID_FEATURE_ASSIGN_ASSET(BAD_REQUEST, "%s Feature에 할당된 에셋이 존재하지 않습니다."),
    DEVICE_MISMATCH(BAD_REQUEST, "요청한 디바이스가 현재 피처에 할당된 디바이스와 일치하지 않습니다."),

    DUPLICATE_USERNAME(BAD_REQUEST, "%s는 이미 존재 하는 아이디 입니다."),
    DUPLICATE_ROLE_NAME(BAD_REQUEST, "%s는 이미 존재하는 Role 이름 입니다."),
    DUPLICATE_ASSET_CODE(BAD_REQUEST, "코드가 %s인 에셋이 이미 존재합니다."),
    DUPLICATE_ASSET_NAME(BAD_REQUEST, "이름이 %s인 에셋이 이미 존재합니다."),
    DUPLICATE_ASSET_CATEGORY_CODE(BAD_REQUEST, "코드가 %s인 에셋 카테고리가 이미 존재합니다."),
    DUPLICATE_FEATURE_ID(BAD_REQUEST, "ID가 %s인 Feature가 이미 존재합니다."),
    DUPLICATE_FACILITY_CODE(BAD_REQUEST, "코드가 %s인 시설이 이미 존재합니다."),
    DUPLICATE_LINE_NAME(BAD_REQUEST, "이름이 %s인 노선이 이미 존재합니다."),

    NOT_FOUND_STATION(NOT_FOUND, "ID가 %s인 역을 찾을 수 없습니다."),
    NOT_FOUND_CATEGORY(NOT_FOUND, "ID가 %s인 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FILE(NOT_FOUND, "ID가 %s인 파일을 찾을 수 없습니다."),
    NOT_FOUND_ASSET(NOT_FOUND, "ID가 %s인 에셋을 찾을 수 없습니다."),
    NOT_FOUND_ASSET_BY_CODE(NOT_FOUND, "코드가 %s인 에셋을 찾을 수 없습니다."),
    NOT_FOUND_ASSET_CATEGORY(NOT_FOUND, "ID가 %s인 에셋 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FEATURE(NOT_FOUND, "ID가 %s인 Feature를 찾을 수 없습니다."),
    NOT_FOUND_DEVICE(NOT_FOUND, "ID가 %s인 디바이스를 찾을 수 없습니다."),
    NOT_FOUND_FACILITY_CATEGORY(NOT_FOUND, "ID가 %s인 시설 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FACILITY_PARENT_CATEGORY(NOT_FOUND, "ID가 %s인 facility의 상위 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FACILITY_CODE(NOT_FOUND, "코드가 %s인 시설을 찾을 수 없습니다."),
    NOT_FOUND_FACILITY(NOT_FOUND, "ID가 %s인 시설을 찾을 수 없습니다."),
    NOT_FOUND_LINE(NOT_FOUND, "ID가 %s인 노선을 찾을 수 없습니다."),
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
