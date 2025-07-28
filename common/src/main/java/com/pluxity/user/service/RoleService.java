package com.pluxity.user.service;

import com.pluxity.global.utils.SortUtils;
import com.pluxity.user.dto.RoleCreateRequest;
import com.pluxity.user.dto.RoleResponse;
import com.pluxity.user.dto.RoleUpdateRequest;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.RolePermission;
import com.pluxity.user.repository.RolePermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public Long save(RoleCreateRequest request) {

        Role role = Role.builder().name(request.name()).description(request.description()).build();

        roleRepository.save(role);

        if (request.permissionIds() != null) {
            for (Long permissionId : request.permissionIds()) {
                Permission permission = permissionService.findById(permissionId);
                rolePermissionRepository.save(
                        RolePermission.builder().permission(permission).role(role).build());
            }
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

        if (request.permissionIds() != null) {
            syncPermissions(role, request.permissionIds());
        }
    }

    private void syncPermissions(Role role, List<Long> requestedPermissionIds) {

        // Step 1: 요청된 모든 권한(Permission) 객체를 준비합니다.
        List<Permission> permissions = permissionService.findAllByIds(requestedPermissionIds);
        Set<Permission> requestedPermissionSet = new HashSet<>(permissions);

        // Step 2: 현재 역할(Role)이 가지고 있는 실제 권한(Permission) 객체 목록을 가져옵니다.
        Map<Long, RolePermission> currentRolePermissionMap =
                role.getRolePermissions().stream()
                        .collect(Collectors.toMap(rp -> rp.getPermission().getId(), rp -> rp));
        Set<Permission> currentPermissionSet =
                currentRolePermissionMap.values().stream()
                        .map(RolePermission::getPermission)
                        .collect(Collectors.toSet());

        // Step 3: 삭제해야 할 권한을 찾아서 RolePermission 연결을 끊습니다.
        // (현재 권한 목록에는 있지만, 요청된 권한 목록에는 없는 것)
        Set<Permission> permissionsToRemove =
                currentPermissionSet.stream()
                        .filter(p -> !requestedPermissionSet.contains(p))
                        .collect(Collectors.toSet());

        if (!permissionsToRemove.isEmpty()) {
            List<RolePermission> rolePermissionsToRemove =
                    permissionsToRemove.stream().map(p -> currentRolePermissionMap.get(p.getId())).toList();
            rolePermissionRepository.deleteAll(rolePermissionsToRemove);
            rolePermissionsToRemove.forEach(role.getRolePermissions()::remove);
        }

        // Step 4: 추가해야 할 권한을 찾아서 새로운 RolePermission을 생성합니다.
        // (요청된 권한 목록에는 있지만, 현재 권한 목록에는 없는 것)
        requestedPermissionSet.stream()
                .filter(p -> !currentPermissionSet.contains(p))
                .forEach(
                        permissionToAdd -> {
                            RolePermission newRolePermission =
                                    RolePermission.builder().role(role).permission(permissionToAdd).build();
                            rolePermissionRepository.save(newRolePermission);
                            role.getRolePermissions().add(newRolePermission);
                        });
    }

    @Transactional
    public void delete(Long id) {
        Role role = findRoleById(id);
        rolePermissionRepository.deleteAllByRole(role);
        userRoleRepository.deleteAllByRole(role);
        roleRepository.delete(role);
    }

    private Role findRoleById(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }
}
