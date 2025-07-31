package com.pluxity.permission;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.permission.dto.PermissionGroupCreateRequest;
import com.pluxity.permission.dto.PermissionGroupResponse;
import com.pluxity.permission.dto.PermissionGroupUpdateRequest;
import com.pluxity.user.repository.RolePermissionRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionGroupService {
    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    public Long create(PermissionGroupCreateRequest request) {
        String permissionGroupName = request.name();

        if (permissionGroupRepository.existsByName(permissionGroupName)) {
            throw new CustomException(ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME, permissionGroupName);
        }

        PermissionGroup permissionGroup =
                PermissionGroup.builder()
                        .name(permissionGroupName)
                        .description(request.description())
                        .build();

        request
                .permissions()
                .forEach(
                        permissionRequest -> {
                            ResourceType resourceType = ResourceType.fromString(permissionRequest.resourceType());
                            String resourceName = resourceType.name();
                            List<String> resourceIds = permissionRequest.resourceIds();

                            if (resourceIds.size() != new HashSet<>(resourceIds).size()) {
                                throw new CustomException(
                                        ErrorCode.DUPLICATE_RESOURCE_ID,
                                        "리소스 타입 '" + resourceName + "'에 중복된 ID가 포함되어 있습니다.");
                            }

                            resourceIds.forEach(
                                    id -> {
                                        Permission permission =
                                                Permission.builder().resourceName(resourceName).resourceId(id).build();
                                        permissionGroup.addPermission(permission);
                                    });
                        });

        PermissionGroup savedGroup = permissionGroupRepository.save(permissionGroup);

        return savedGroup.getId();
    }

    @Transactional(readOnly = true)
    public PermissionGroupResponse findById(Long id) {
        PermissionGroup permissionGroup = findPermissionGroupById(id);
        return PermissionGroupResponse.from(permissionGroup);
    }

    @Transactional(readOnly = true)
    public List<PermissionGroupResponse> findAll() {
        List<PermissionGroup> permissionGroups = permissionGroupRepository.findAll();
        return permissionGroups.stream()
                .map(PermissionGroupResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, PermissionGroupUpdateRequest request) {
        PermissionGroup permissionGroup = findPermissionGroupById(id);

        String groupName = request.name();
        if (groupName != null && !groupName.isBlank() && !groupName.equals(permissionGroup.getName())) {
            if (permissionGroupRepository.existsByNameAndIdNot(groupName, id)) {
                throw new CustomException(ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME, groupName);
            }
            permissionGroup.changeName(groupName);
        }
        if (request.description() != null) {
            permissionGroup.changeDescription(request.description());
        }

        // 현재 권한을 "ResourceType:ResourceId" 형태의 키를 가진 Map으로 변환
        Map<String, Permission> existingPermissionsMap =
                permissionGroup.getPermissions().stream()
                        .collect(
                                Collectors.toMap(
                                        p -> p.getResourceName() + ":" + p.getResourceId(), Function.identity()));

        // 요청된 권한을 "ResourceType:ResourceId" 형태의 키를 가진 Set으로 변환
        Set<String> requestedPermissionKeys = new HashSet<>();
        request
                .permissions()
                .forEach(
                        pr -> {
                            String resourceName = ResourceType.fromString(pr.resourceType()).name();
                            pr.resourceIds()
                                    .forEach(resId -> requestedPermissionKeys.add(resourceName + ":" + resId));
                        });

        // 삭제할 권한을 찾아 제거 (현재 맵의 키 목록에는 있지만, 요청된 키 Set에는 없는 권한)
        existingPermissionsMap.forEach(
                (key, permission) -> {
                    if (!requestedPermissionKeys.contains(key)) {
                        permissionGroup.removePermission(permission);
                        permissionRepository.delete(permission);
                    }
                });

        // 추가할 권한을 찾아 생성 및 추가
        request
                .permissions()
                .forEach(
                        pr -> {
                            String resourceName = ResourceType.fromString(pr.resourceType()).name();
                            pr.resourceIds()
                                    .forEach(
                                            resId -> {
                                                String key = resourceName + ":" + resId;
                                                if (!existingPermissionsMap.containsKey(key)) {
                                                    Permission newPermission =
                                                            Permission.builder()
                                                                    .resourceName(resourceName)
                                                                    .resourceId(resId)
                                                                    .build();
                                                    permissionGroup.addPermission(newPermission);
                                                }
                                            });
                        });
    }

    @Transactional
    public void delete(Long id) {
        PermissionGroup permissionGroup = findPermissionGroupById(id);
        rolePermissionRepository.deleteAllByPermissionGroup(permissionGroup);
        permissionRepository.deleteAll(permissionGroup.getPermissions());
        permissionGroupRepository.delete(permissionGroup);
    }

    public PermissionGroup findPermissionGroupById(Long id) {
        return permissionGroupRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PERMISSION_GROUP, id));
    }
}
