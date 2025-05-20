package com.pluxity.domains.acl.service;

import com.pluxity.domains.device_category_acl.device.dto.GrantPermissionRequest;
import com.pluxity.domains.device_category_acl.device.dto.RevokePermissionRequest;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/** 엔티티 ACL 관리를 담당하는 클래스 (컴포지션 패턴) */
@Transactional
public class EntityAclManager implements EntityAclOperations {

    private final AclManagerService aclManagerService;

    public EntityAclManager(AclManagerService aclManagerService) {
        this.aclManagerService = aclManagerService;
    }

    /** 요청 타입과 엔티티 타입이 일치하는지 검증 */
    @Override
    public void validateEntityType(String requestType, String entityType) {
        if (!entityType.equalsIgnoreCase(requestType)) {
            throw new IllegalArgumentException("Target type must be " + entityType);
        }
    }

    /** 문자열 권한 목록을 Permission 객체 목록으로 변환 */
    @Override
    public List<Permission> convertToPermissions(List<String> permissionStrings, boolean forRevoke) {
        if (permissionStrings == null || permissionStrings.isEmpty()) {
            // 권한 회수인 경우 빈 목록 그대로 반환 (호출자가 처리)
            if (forRevoke) {
                return Collections.emptyList();
            }

            // 권한 부여인 경우 기본 CRUD 권한 반환
            return List.of(
                    BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE);
        }

        return permissionStrings.stream()
                .map(PermissionMapper::fromString)
                .collect(Collectors.toList());
    }

    /** 문자열 권한 목록을 Permission 객체 목록으로 변환 (권한 부여용) */
    @Override
    public List<Permission> convertToPermissions(List<String> permissionStrings) {
        return convertToPermissions(permissionStrings, false);
    }

    /** 권한 부여 */
    @Override
    public void grantPermission(
            GrantPermissionRequest request, String entityType, Class<?> entityClass) {
        validateEntityType(request.targetType(), entityType);

        List<Permission> permissions = convertToPermissions(request.permissions());

        if (request.isRole()) {
            aclManagerService.addPermissionsForRole(
                    entityClass, request.targetId(), request.principalName(), permissions);
        } else {
            aclManagerService.addPermissionsForUser(
                    entityClass, request.targetId(), request.principalName(), permissions);
        }
    }

    /** 권한 회수 */
    @Override
    public void revokePermission(
            RevokePermissionRequest request, String entityType, Class<?> entityClass) {
        validateEntityType(request.targetType(), entityType);

        // ADMIN 역할의 권한은 회수하지 않음
        if (request.isRole() && "ROLE_ADMIN".equalsIgnoreCase(request.principalName())) {
            throw new IllegalStateException("ADMIN 역할의 권한은 회수할 수 없습니다.");
        }

        // 모든 권한 제거 요청인 경우
        if (request.removeAll()) {
            if (request.isRole()) {
                aclManagerService.removeAllPermissionsForRole(
                        entityClass, request.targetId(), request.principalName());
            } else {
                aclManagerService.removeAllPermissionsForUser(
                        entityClass, request.targetId(), request.principalName());
            }
            return;
        }

        // 권한 목록이 비어있거나 null인 경우, 기본 CRUD 권한을 회수
        if (request.permissions() == null || request.permissions().isEmpty()) {
            List<Permission> defaultPermissionsToRevoke =
                    List.of(
                            BasePermission.READ,
                            BasePermission.WRITE,
                            BasePermission.CREATE,
                            BasePermission.DELETE);

            if (request.isRole()) {
                aclManagerService.removePermissionsForRole(
                        entityClass, request.targetId(), request.principalName(), defaultPermissionsToRevoke);
            } else {
                aclManagerService.removePermissionsForUser(
                        entityClass, request.targetId(), request.principalName(), defaultPermissionsToRevoke);
            }
            return;
        }

        // 특정 권한만 제거 요청인 경우
        List<Permission> permissionsToRevoke = convertToPermissions(request.permissions(), true);

        if (request.isRole()) {
            aclManagerService.removePermissionsForRole(
                    entityClass, request.targetId(), request.principalName(), permissionsToRevoke);
        } else {
            aclManagerService.removePermissionsForUser(
                    entityClass, request.targetId(), request.principalName(), permissionsToRevoke);
        }
    }

    /**
     * 현재 사용자가 접근 가능한 엔티티 목록 필터링 주의: 권한 확인이 각 엔티티마다 개별적으로 수행되기 때문에 큰 목록의 경우 N+1 쿼리 문제가 발생할 수 있습니다. 대량의
     * 엔티티를 처리할 경우 더 최적화된 방법을 고려해야 합니다.
     */
    @Override
    public <T, R> List<R> findAllAllowedForCurrentUser(
            List<T> entities,
            Function<T, ? extends Serializable> idExtractor,
            Function<T, R> dtoConverter,
            Class<?> entityClass) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptyList();
        }

        // ADMIN은 모든 엔티티 접근 가능
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            return entities.stream().map(dtoConverter).collect(Collectors.toList());
        }

        // 읽기 권한이 있는 엔티티만 필터링
        String username = authentication.getName();

        // 성능 최적화: 권한 결과를 캐싱
        // 이 방법은 메모리 사용을 약간 증가시키지만 같은 엔티티에 대한 중복 권한 확인을 방지합니다
        Map<Serializable, Boolean> permissionCache = new HashMap<>();

        return entities.stream()
                .filter(
                        entity -> {
                            Serializable id = idExtractor.apply(entity);

                            // 캐시에서 권한 결과 확인
                            return permissionCache.computeIfAbsent(
                                    id,
                                    entityId ->
                                            aclManagerService.hasPermissionForUser(
                                                    entityClass, entityId, username, BasePermission.READ));
                        })
                .map(dtoConverter)
                .collect(Collectors.toList());
    }

    /** 사용자가 해당 엔티티에 접근 권한이 있는지 확인 */
    @Override
    public boolean hasReadPermission(Serializable entityId, Class<?> entityClass, String username) {
        return aclManagerService.hasPermissionForUser(
                entityClass, entityId, username, BasePermission.READ);
    }
}
