package com.pluxity.domains.acl.service;

import com.pluxity.domains.device_category_acl.device.dto.GrantPermissionRequest;
import com.pluxity.domains.device_category_acl.device.dto.RevokePermissionRequest;
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

    /** 권한 부여 */
    void grantPermission(GrantPermissionRequest request, String entityType, Class<?> entityClass);

    /** 권한 회수 */
    void revokePermission(RevokePermissionRequest request, String entityType, Class<?> entityClass);

    /** 현재 사용자가 접근 가능한 엔티티 목록 필터링 */
    <T, R> List<R> findAllAllowedForCurrentUser(
            List<T> entities,
            Function<T, ? extends Serializable> idExtractor,
            Function<T, R> dtoConverter,
            Class<?> entityClass);

    /** 사용자가 해당 엔티티에 접근 권한이 있는지 확인 */
    boolean hasReadPermission(Serializable entityId, Class<?> entityClass, String username);
}
