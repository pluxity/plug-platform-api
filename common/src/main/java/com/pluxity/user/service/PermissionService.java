package com.pluxity.user.service;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.PermissionRequest;
import com.pluxity.user.entity.ResourcePermission;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.ResourcePermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleRepository roleRepository;
    private final ResourcePermissionRepository permissionRepository;

    @Transactional
    public void grantPermissionToRole(PermissionRequest request) {
        String resourceName = request.resourceName().getResourceName();
        ResourceType resourceType = ResourceType.fromString(resourceName);

        request
                .resourceId()
                .forEach(
                        resourceId -> {
                            Role role =
                                    roleRepository
                                            .findById(request.roleId())
                                            .orElseThrow(
                                                    () ->
                                                            new CustomException(ErrorCode.NOT_FOUND_ROLE, request.resourceId()));

                            if (permissionRepository.existsByRoleAndResourceNameAndResourceId(
                                    role, resourceName, resourceId)) {
                                return;
                            }

                            ResourcePermission newPermission =
                                    ResourcePermission.builder()
                                            .role(role)
                                            .resourceName(resourceName)
                                            .resourceId(resourceId)
                                            .build();
                            permissionRepository.save(newPermission);
                        });
    }

    @Transactional
    public void revokePermissionFromRole(PermissionRequest request) {
        String resourceName = request.resourceName().getResourceName();
        ResourceType resourceType = ResourceType.fromString(resourceName);

        request
                .resourceId()
                .forEach(
                        resourceId -> {
                            Role role =
                                    roleRepository
                                            .findById(request.roleId())
                                            .orElseThrow(
                                                    () ->
                                                            new CustomException(ErrorCode.NOT_FOUND_ROLE, request.resourceId()));

                            permissionRepository.deleteByRoleAndResourceNameAndResourceId(
                                    role, resourceName, resourceId);
                        });
    }

    @Transactional
    public void syncPermissions(PermissionRequest request) {
        ResourceType resourceType = ResourceType.fromString(request.resourceName().getResourceName());

        Role role =
                roleRepository
                        .findById(request.roleId())
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROLE, request.roleId()));

        List<Long> currentDbResourceIds =
                permissionRepository.findResourceIdsByRoleAndResourceName(
                        role, resourceType.getResourceName());

        List<Long> idsToRemove =
                currentDbResourceIds.stream().filter(id -> !request.resourceId().contains(id)).toList();

        if (!idsToRemove.isEmpty()) {
            permissionRepository.deleteByRoleAndResourceNameAndResourceIdIn(
                    role, resourceType.getResourceName(), idsToRemove);
        }

        List<Long> idsToAdd =
                request.resourceId().stream().filter(id -> !currentDbResourceIds.contains(id)).toList();

        if (!idsToAdd.isEmpty()) {
            List<ResourcePermission> permissionsToSave =
                    idsToAdd.stream()
                            .map(
                                    id ->
                                            ResourcePermission.builder()
                                                    .role(role)
                                                    .resourceName(resourceType.getResourceName())
                                                    .resourceId(id)
                                                    .build())
                            .collect(Collectors.toList());
            permissionRepository.saveAll(permissionsToSave);
        }
    }
}
