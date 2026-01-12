package com.pluxity.global.constant

import org.springframework.http.HttpStatus

enum class ErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String,
) : Code {
    INVALID_ID_OR_PASSWORD(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 틀렸습니다."),

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "ACCESS 토큰이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "REFRESH 토큰이 유효하지 않습니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 형식입니다."),

    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "적절하지 않은 파일 유형입니다."),
    INVALID_FILE_STATUS(HttpStatus.BAD_REQUEST, "적절하지 않은 파일 상태입니다."),
    INVALID_SBM_FILE(HttpStatus.BAD_REQUEST, "적절하지 않은 SBM 파일입니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),

    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "ACCESS 토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "REFRESH 토큰이 만료되었습니다."),

    FAILED_TO_ZIP_FILE(HttpStatus.BAD_REQUEST, "파일 압축에 실패했습니다."),
    FAILED_TO_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FAILED_TO_PROCESS_SBM_FILE(HttpStatus.BAD_REQUEST, "SBM 파일 처리에 실패했습니다."),

    DUPLICATE_RESOURCE_ID(HttpStatus.BAD_REQUEST, "중복된 리소스 ID가 포함되어 있습니다."),

    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "%s 을(를) 가진 회원을 찾을 수 없습니다."),
    NOT_FOUND_DATA(HttpStatus.BAD_REQUEST, "데이터가 존재하지 않습니다."),
    NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "권한이 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_CANNOT_ACCESS(HttpStatus.UNAUTHORIZED, "사용자 '%s'는 ID가 %d인 %s에 접근할 권한이 없습니다."),
    NOT_FOUND_RESOURCE_ID(HttpStatus.BAD_REQUEST, "요청한 리소스 ID가 존재하지 않습니다."),
    INVALID_RESOURCE_TYPE(HttpStatus.BAD_REQUEST, "%s는 유효하지 않은 RESOURCE TYPE 입니다."),

    CATEGORY_HAS_DEVICES(HttpStatus.BAD_REQUEST, "카테고리에 등록된 디바이스가 있어 삭제할 수 없습니다."),

    EXCEED_CATEGORY_DEPTH(HttpStatus.BAD_REQUEST, "카테고리는 깊이를 초과했습니다"),
    INVALID_REFERENCE(HttpStatus.BAD_REQUEST, "요청 된 참조가 유효하지 않습니다."),

    INVALID_PARENT_CATEGORY(HttpStatus.BAD_REQUEST, "카테고리는 자기 자신을 부모로 가질 수 없습니다."),
    CIRCULAR_REFERENCE_CATEGORY(HttpStatus.BAD_REQUEST, "하위 카테고리를 부모 카테고리로 지정할 수 없습니다."),

    CATEGORY_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "카테고리에 하위 카테고리가 있어 삭제할 수 없습니다."),
    ASSET_CATEGORY_HAS_ASSET(HttpStatus.BAD_REQUEST, "에셋 카테고리에 에셋이 있어 삭제할 수 없습니다."),
    FACILITY_CATEGORY_HAS_FACILITY(HttpStatus.BAD_REQUEST, "시설 카테고리에 시설이 있어 삭제할 수 없습니다."),
    CCTV_MISMATCH(HttpStatus.BAD_REQUEST, "요청한 디바이스가 현재 피처에 할당된 디바이스와 일치하지 않습니다."),

    FEATURE_HAS_NOT_DEVICE(HttpStatus.BAD_REQUEST, "%s Feature에 할당된 디바이스가 존재하지 않습니다."),
    NOT_EXIST_ASSET_CATEGORY(HttpStatus.BAD_REQUEST, "%s 아이디 에셋에 카테고리가 존재하지 않습니다."),
    INVALID_FEATURE_ASSIGN_ASSET(HttpStatus.BAD_REQUEST, "%s Feature에 할당된 에셋이 존재하지 않습니다."),
    DEVICE_MISMATCH(HttpStatus.BAD_REQUEST, "요청한 디바이스가 현재 피처에 할당된 디바이스와 일치하지 않습니다."),
    DEVICE_NOT_ASSIGNED(HttpStatus.BAD_REQUEST, "%s 디바이스에 할당된 피처가 존재하지 않습니다."),
    CCTV_NOT_ASSIGNED(HttpStatus.BAD_REQUEST, "%s Cctv에 할당된 피처가 존재하지 않습니다."),
    ALREADY_ASSIGNED_TARGET(HttpStatus.BAD_REQUEST, "ID(%s) %s에 이미 할당된 피처가 존재합니다."),
    ALREADY_FEATURE_ASSIGNED(HttpStatus.BAD_REQUEST, "%s는 이미 다른 대상에 연결되어 있습니다."),

    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, "%s는 이미 존재 하는 아이디 입니다."),
    DUPLICATE_ROLE_NAME(HttpStatus.BAD_REQUEST, "%s는 이미 존재하는 Role 이름 입니다."),
    DUPLICATE_ASSET_CODE(HttpStatus.BAD_REQUEST, "코드가 %s인 에셋이 이미 존재합니다."),
    DUPLICATE_ASSET_NAME(HttpStatus.BAD_REQUEST, "이름이 %s인 에셋이 이미 존재합니다."),
    DUPLICATE_ASSET_CATEGORY_CODE(HttpStatus.BAD_REQUEST, "코드가 %s인 에셋 카테고리가 이미 존재합니다."),
    DUPLICATE_FEATURE_ID(HttpStatus.BAD_REQUEST, "ID가 %s인 Feature가 이미 존재합니다."),
    DUPLICATE_FACILITY_CODE(HttpStatus.BAD_REQUEST, "코드가 %s인 시설이 이미 존재합니다."),
    DUPLICATE_LINE_NAME(HttpStatus.BAD_REQUEST, "이름이 %s인 노선이 이미 존재합니다."),
    DUPLICATE_PERMISSION_NAME_ID(HttpStatus.BAD_REQUEST, "resource name이 %s이고 id가 %s인 Permission이 이미 존재합니다."),
    DUPLICATE_PERMISSION_NAME(HttpStatus.BAD_REQUEST, "이름이 %s인 권한이 이미 존재합니다."),
    DUPLICATE_FEATURE_OTHER_CCTV(HttpStatus.BAD_REQUEST, "피쳐 ID [%s]는 이미 다른 CCTV에 할당되어 있습니다."),
    DUPLICATE_FEATURE_OTHER_DEVICE(HttpStatus.BAD_REQUEST, "피쳐 ID [%s]는 이미 다른 디바이스에 할당되어 있습니다."),

    NOT_FOUND_STATION(HttpStatus.NOT_FOUND, "ID가 %s인 역을 찾을 수 없습니다."),
    NOT_FOUND_BUILDING(HttpStatus.NOT_FOUND, "ID가 %s인 건물을 찾을 수 없습니다."),
    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND, "ID가 %s인 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FILE(HttpStatus.NOT_FOUND, "ID가 %s인 파일을 찾을 수 없습니다."),
    NOT_FOUND_ASSET(HttpStatus.NOT_FOUND, "ID가 %s인 에셋을 찾을 수 없습니다."),
    NOT_FOUND_ASSET_BY_CODE(HttpStatus.NOT_FOUND, "코드가 %s인 에셋을 찾을 수 없습니다."),
    NOT_FOUND_ASSET_CATEGORY(HttpStatus.NOT_FOUND, "ID가 %s인 에셋 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FEATURE(HttpStatus.NOT_FOUND, "ID가 %s인 Feature를 찾을 수 없습니다."),
    NOT_FOUND_DEVICE(HttpStatus.NOT_FOUND, "ID가 %s인 디바이스를 찾을 수 없습니다."),
    NOT_FOUND_DEVICE_BY_FEATURE(HttpStatus.NOT_FOUND, "Feature ID가 %s인 디바이스를 찾을 수 없습니다."),
    NOT_FOUND_FACILITY_CATEGORY(HttpStatus.NOT_FOUND, "ID가 %s인 시설 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FACILITY_PARENT_CATEGORY(HttpStatus.NOT_FOUND, "ID가 %s인 facility의 상위 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_FACILITY_CODE(HttpStatus.NOT_FOUND, "코드가 %s인 시설을 찾을 수 없습니다."),
    NOT_FOUND_FACILITY(HttpStatus.NOT_FOUND, "ID가 %s인 시설을 찾을 수 없습니다."),
    NOT_FOUND_FACILITY_PATH(HttpStatus.NOT_FOUND, "ID가 %s인 경로를 찾을 수 없습니다."),
    NOT_FOUND_LINE(HttpStatus.NOT_FOUND, "ID가 %s인 노선을 찾을 수 없습니다."),
    NOT_FOUND_PATH_TYPE(HttpStatus.NOT_FOUND, "타입 %s를 찾을 수 없습니다."),
    NOT_FOUND_ROLE(HttpStatus.NOT_FOUND, "ID가 %s인 Role을 찾을 수 없습니다."),
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND, "ID가 %s인 %s 리소스를 찾을 수 없습니다."),
    NOT_FOUND_PARK(HttpStatus.NOT_FOUND, "ID가 %s인 Park를 찾을 수 없습니다."),
    NOT_FOUND_PERMISSIONS(HttpStatus.NOT_FOUND, "%s인 ids로 모든 권한을 찾을 수 없습니다"),
    NOT_FOUND_STATION_LINE(HttpStatus.NOT_FOUND, "ID가 %s인 Station 또는 ID가 %s인 Line을 찾을 수 없습니다."),
    NOT_FOUND_PERMISSION(HttpStatus.NOT_FOUND, "ID가 %s인 Permission을 찾을 수 없습니다."),
    NOT_FOUND_CCTV_CATEGORY(HttpStatus.NOT_FOUND, "ID가 %s인 CCTV 카테고리를 찾을 수 없습니다."),
    NOT_FOUND_CCTV(HttpStatus.NOT_FOUND, "ID가 %s인 CCTV를 찾을 수 없습니다."),
    NOT_FOUND_CCTV_BY_FEATURE(HttpStatus.NOT_FOUND, "Feature ID가 %s인 CCTV를 찾을 수 없습니다."),
    NOT_FOUND_LABEL_3D(HttpStatus.NOT_FOUND, "ID가 %s인 3D 라벨을 찾을 수 없습니다."),

    INVALID_RESOURCE_IDS_INCLUDED(HttpStatus.BAD_REQUEST, "요청한 리소스 ID %s 는 유효하지 않습니다."),
    FAILED_TO_SAVE_ENTITY(HttpStatus.INTERNAL_SERVER_ERROR, "엔티티 저장에 실패했습니다."),

    MEDIAMTX_ADD_ERROR(HttpStatus.BAD_REQUEST, "MediaMtx등록 실패: %s"),
    MEDIAMTX_DELETE_ERROR(HttpStatus.BAD_REQUEST, "MediaMtx삭제 실패: %s"),

    INVALID_DEVICE_ACTION(HttpStatus.BAD_REQUEST, "%s 은(는) %s 을(를) 수행할 수 없습니다."),
    UNMATCHED_FACILITY_SCENE(HttpStatus.BAD_REQUEST, "요청한 Scene이 해당 시설에 속하지 않습니다."),
    NOT_FOUND_SCENE(HttpStatus.NOT_FOUND, "ID가 %s인 Scene을 찾을 수 없습니다."),
    NOT_FOUND_ACTION(HttpStatus.NOT_FOUND, "Scene에 등록되지 않은 Action 입니다."),
    INVALID_DEVICE_TYPE(HttpStatus.BAD_REQUEST, "요청한 Device Type은 유효하지 않습니다."),
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus

    override fun getMessage(): String = message

    override fun getStatusName(): String = httpStatus.name
}
