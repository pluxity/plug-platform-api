package com.pluxity.user.dto;

import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.RolePermission;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record RoleResponse(
        Long id, String name, String description, List<PermissionResponse> permissions) {
    public static RoleResponse from(Role role) {
        if (role == null) {
            return null;
        }

        // 1. Role에 연결된 모든 Permission 엔티티를 가져옵니다.
        List<Permission> allPermissions =
                role.getRolePermissions().stream().map(RolePermission::getPermission).toList();

        // 2. Stream의 groupingBy를 사용하여 resourceName을 키로, resourceId 목록을 값으로 하는 Map을 생성합니다.
        Map<String, List<String>> groupedPermissions =
                allPermissions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        Permission::getResourceName, // 그룹화할 기준 (키)
                                        Collectors.mapping(
                                                Permission::getResourceId, Collectors.toList()) // 그룹화된 요소들을 어떻게 변환할지 (값)
                                        ));

        // 3. 그룹화된 Map을 최종적인 List<PermissionResponse> 형태로 변환합니다.
        List<PermissionResponse> permissionResponses =
                groupedPermissions.entrySet().stream()
                        .map(entry -> new PermissionResponse(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

        // 4. 최종적으로 그룹화된 권한 목록을 포함하여 RoleResponse 객체를 생성합니다.
        return new RoleResponse(
                role.getId(), role.getName(), role.getDescription(), permissionResponses);
    }
}
