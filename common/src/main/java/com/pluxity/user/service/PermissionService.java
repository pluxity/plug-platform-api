package com.pluxity.user.service;

import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public Permission findOrCreatePermission(ResourceType resourceType, Long resourceId) {
        String resourceNameStr = resourceType.getResourceName();
        return permissionRepository
                .findByResourceNameAndResourceId(resourceNameStr, resourceId)
                .orElseGet(
                        () ->
                                permissionRepository.save(
                                        Permission.builder()
                                                .resourceName(resourceNameStr)
                                                .resourceId(resourceId)
                                                .build()));
    }
}
