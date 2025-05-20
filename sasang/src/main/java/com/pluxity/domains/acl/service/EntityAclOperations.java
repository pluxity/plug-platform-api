package com.pluxity.domains.acl.service;

import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import org.springframework.security.acls.model.Permission;

/** 엔티티 ACL 관리 작업을 위한 인터페이스 */
public interface EntityAclOperations {

    /** 요청 타입과 엔티티 타입이 일치하는지 검증 */
    void validateEntityType(String requestType, String entityType);

    /** 문자열 권한 목록을 Permission 객체 목록으로 변환 */
    List<Permission> convertToPermissions(List<String> permissionStrings);

    /** 문자열 권한 목록을 Permission 객체 목록으로 변환 */
    List<Permission> convertToPermissions(List<String> permissionStrings, boolean forRevoke);

    /**
     * 권한 부여/회수 통합 처리 하나의 대상 유형(targetType)과 주체(principalName)에 대해 여러 대상 ID(targetId)에 권한 부여(GRANT) 또는
     * 회수(REVOKE) 작업을 수행합니다.
     *
     * @param request 권한 부여/회수 요청 DTO
     * @param entityType 엔티티 유형 (검증용)
     * @param entityClass 엔티티 클래스
     */
    void managePermission(PermissionRequestDto request, String entityType, Class<?> entityClass);

    /**
     * 현재 사용자가 접근 가능한 엔티티 목록 필터링
     *
     * @param entities 원본 엔티티 목록
     * @param idExtractor 엔티티에서 ID를 추출하는 함수
     * @param dtoConverter 엔티티를 DTO로 변환하는 함수
     * @param entityClass 엔티티 클래스
     * @return 접근 권한이 있는 엔티티들을 DTO로 변환한 목록
     */
    <T, R> List<R> findAllAllowedForCurrentUser(
            List<T> entities,
            Function<T, ? extends Serializable> idExtractor,
            Function<T, R> dtoConverter,
            Class<?> entityClass);

    /**
     * 사용자가 해당 엔티티에 접근 권한이 있는지 확인
     *
     * @param entityId 엔티티 ID
     * @param entityClass 엔티티 클래스
     * @param username 사용자 이름
     * @return 접근 권한 여부
     */
    boolean hasReadPermission(Serializable entityId, Class<?> entityClass, String username);
}
