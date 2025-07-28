package com.pluxity.user.service;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.SortUtils;
import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.dto.PermissionResponse;
import com.pluxity.user.dto.PermissionUpdateRequest;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.repository.RolePermissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    public Long create(PermissionCreateRequest request) {
        String resourceName = request.resourceName().getResourceName();
        if (permissionRepository.existsByResourceNameAndResourceId(
                resourceName, request.resourceId())) {
            throw new CustomException(
                    ErrorCode.DUPLICATE_PERMISSION_NAME_ID, resourceName, request.resourceId());
        }
        Permission permission =
                Permission.builder().resourceName(resourceName).resourceId(request.resourceId()).build();
        return permissionRepository.save(permission).getId();
    }

    @Transactional(readOnly = true)
    public Permission findById(Long id) {
        return permissionRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PERMISSION, id));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
                .map(PermissionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Permission> findAllByIds(List<Long> ids) {
        List<Permission> permissions = permissionRepository.findAllById(ids);
        if (permissions.size() != ids.size()) {
            throw new CustomException(ErrorCode.NOT_FOUND_PERMISSIONS, ids.toString());
        }
        return permissions;
    }

    @Transactional
    public void update(Long id, PermissionUpdateRequest request) {
        Permission permission = findById(id);

        if (request.resourceName() != null) {
            permission.changeResourceName(request.resourceName().getResourceName());
        }
        if (request.resourceId() != null && !request.resourceId().isBlank()) {
            permission.changeResourceId(request.resourceId());
        }
    }

    @Transactional
    public void delete(Long id) {
        Permission permission = findById(id);
        rolePermissionRepository.deleteAllByPermission(permission);
        permissionRepository.delete(permission);
    }
}
