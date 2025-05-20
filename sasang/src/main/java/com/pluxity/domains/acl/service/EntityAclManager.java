package com.pluxity.domains.acl.service;

import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto.PermissionOperation;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto.PermissionTarget;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 엔티티 ACL 관리를 담당하는 클래스 (컴포지션 패턴) */
@Service
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
        return List.of(
                BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE);
    }

    /** 문자열 권한 목록을 Permission 객체 목록으로 변환 (권한 부여용) */
    @Override
    public List<Permission> convertToPermissions(List<String> permissionStrings) {
        return List.of(
                BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE);
    }

    /** 권한 부여/회수 통합 처리 */
    @Override
    public void managePermission(
            PermissionRequestDto request, String entityType, Class<?> entityClass) {
        validateEntityType(request.targetType(), entityType);

        List<Permission> permissions = convertToPermissions(request.getPermissions());
        String principalName = request.principalName();

        // ADMIN 역할 권한 회수 요청 확인
        if ("ROLE_ADMIN".equalsIgnoreCase(principalName)
                && request.targets().stream().anyMatch(t -> t.operation() == PermissionOperation.REVOKE)) {
            throw new IllegalStateException("ADMIN 역할의 권한은 회수할 수 없습니다.");
        }

        // 각 대상에 대해 처리
        for (PermissionTarget target : request.targets()) {
            Long targetId = target.targetId();

            if (target.operation() == PermissionOperation.GRANT) {
                aclManagerService.addPermissionsForRole(entityClass, targetId, principalName, permissions);
            } else if (target.operation() == PermissionOperation.REVOKE) {
                aclManagerService.removePermissionsForRole(
                        entityClass, targetId, principalName, permissions);
            }
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
                                            aclManagerService.hasPermissionForRole(
                                                    entityClass, entityId, username, BasePermission.READ));
                        })
                .map(dtoConverter)
                .collect(Collectors.toList());
    }

    /** 사용자가 해당 엔티티에 접근 권한이 있는지 확인 */
    @Override
    public boolean hasReadPermission(Serializable entityId, Class<?> entityClass, String username) {
        return aclManagerService.hasPermissionForRole(
                entityClass, entityId, username, BasePermission.READ);
    }
}
