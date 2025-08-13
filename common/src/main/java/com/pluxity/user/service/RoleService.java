package com.pluxity.user.service;

import com.pluxity.global.utils.SortUtils;
import com.pluxity.permission.PermissionGroup;
import com.pluxity.permission.PermissionGroupService;
import com.pluxity.permission.PermissionService;
import com.pluxity.user.dto.RoleCreateRequest;
import com.pluxity.user.dto.RoleResponse;
import com.pluxity.user.dto.RoleUpdateRequest;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.RolePermission;
import com.pluxity.user.repository.RolePermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionGroupService permissionGroupService;
    private final EntityManager em;

    @Transactional
    public Long save(RoleCreateRequest request) {

        Role role = Role.builder().name(request.name()).description(request.description()).build();

        roleRepository.save(role);

        if (request.permissionGroupIds() != null && !request.permissionGroupIds().isEmpty()) {
            List<RolePermission> newRolePermissions =
                    request.permissionGroupIds().stream()
                            .map(
                                    permissionGroupId -> {
                                        PermissionGroup permissionGroup =
                                                permissionGroupService.findPermissionGroupById(permissionGroupId);
                                        return RolePermission.builder()
                                                .permissionGroup(permissionGroup)
                                                .role(role)
                                                .build();
                                    })
                            .toList();

            rolePermissionRepository.saveAll(newRolePermissions);
            newRolePermissions.forEach(role::addRolePermission);
        }

        return role.getId();
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return RoleResponse.from(findRoleById(id));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional
    public void update(Long id, RoleUpdateRequest request) {
        Role role = findRoleById(id);

        if (request.name() != null && !request.name().isBlank()) {
            role.changeRoleName(request.name());
        }
        if (request.description() != null) {
            role.changeDescription(request.description());
        }

        if (request.permissionGroupIds() != null) {
            syncPermissionGroups(role, request.permissionGroupIds());
        }
    }

    private void syncPermissionGroups(Role role, List<Long> requestedGroupIds) {
        Set<Long> currentGroupIds =
                role.getRolePermissions().stream()
                        .map(rp -> rp.getPermissionGroup().getId())
                        .collect(Collectors.toSet());
        Set<Long> requestedGroupIdsSet = new HashSet<>(requestedGroupIds);

        List<RolePermission> rolePermissionsToRemove =
                role.getRolePermissions().stream()
                        .filter(rp -> !requestedGroupIdsSet.contains(rp.getPermissionGroup().getId()))
                        .toList();

        if (!rolePermissionsToRemove.isEmpty()) {
            rolePermissionRepository.deleteAllInBatch(rolePermissionsToRemove);
            rolePermissionsToRemove.forEach(role::removeRolePermission);
        }

        Set<Long> idsToAdd =
                requestedGroupIdsSet.stream()
                        .filter(id -> !currentGroupIds.contains(id))
                        .collect(Collectors.toSet());

        if (!idsToAdd.isEmpty()) {
            List<RolePermission> rolePermissionsToAdd =
                    idsToAdd.stream()
                            .map(
                                    groupId -> {
                                        PermissionGroup permissionGroup =
                                                permissionGroupService.findPermissionGroupById(groupId);
                                        return RolePermission.builder()
                                                .role(role)
                                                .permissionGroup(permissionGroup)
                                                .build();
                                    })
                            .toList();

            List<RolePermission> savedRolePermissions =
                    rolePermissionRepository.saveAll(rolePermissionsToAdd);
            savedRolePermissions.forEach(role::addRolePermission);
        }
    }

    @Transactional
    public void delete(Long id) {
        Role role = findRoleById(id);
        rolePermissionRepository.deleteAllByRole(role);
        userRoleRepository.deleteAllByRole(role);
        em.flush();
        em.clear();
        roleRepository.deleteById(role.getId());
    }

    public Role findRoleById(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }
}
