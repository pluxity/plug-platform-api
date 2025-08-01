package com.pluxity.permission;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.permission.dto.PermissionCreateRequest;
import com.pluxity.permission.dto.PermissionUpdateRequest;
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
    public List<Long> create(PermissionCreateRequest request) {
        ResourceType resourceType = ResourceType.fromString(request.resourceName());
        String resourceName = resourceType.name();
        List<String> resourceIds = request.resourceIds();

        List<Permission> permissionsToSave =
                resourceIds.stream()
                        .map(id -> Permission.builder().resourceName(resourceName).resourceId(id).build())
                        .toList();

        List<Permission> savedPermissions = permissionRepository.saveAll(permissionsToSave);

        return savedPermissions.stream().map(Permission::getId).toList();
    }

    @Transactional(readOnly = true)
    public Permission findById(Long id) {
        return permissionRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PERMISSION, id));
    }

    //    @Transactional(readOnly = true)
    //    public List<PermissionResponse> findAll() {
    //        return permissionRepository.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
    //                .map(PermissionResponse::from)
    //                .toList();
    //    }

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
        ResourceType resourceType = ResourceType.fromString(request.resourceName());
        String resourceName = resourceType.name();

        Permission permission = findById(id);

        if (request.resourceName() != null) {
            permission.changeResourceName(resourceName);
        }
        if (request.resourceId() != null && !request.resourceId().isBlank()) {
            permission.changeResourceId(request.resourceId());
        }
    }

    @Transactional
    public void delete(Long id) {
        Permission permission = findById(id);
        rolePermissionRepository.deleteAllByPermissionGroup(permission.getPermissionGroup());
        permissionRepository.delete(permission);
    }
}
