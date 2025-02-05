package com.pluxity.user.service;

import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return RoleResponse.from(findRoleById(id));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(RoleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getRolePermissions(Long roleId) {
        Role role = findRoleById(roleId);
        return role.getPermissions().stream().map(PermissionResponse::from).toList();
    }

    @Transactional
    public RoleResponse save(RoleCreateRequest request) {
        Role role = Role.builder().roleName(request.roleName()).build();

        Role savedRole = roleRepository.save(role);
        return RoleResponse.from(savedRole);
    }

    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        Role role = findRoleById(id);
        role.changeRoleName(request.roleName());
        return RoleResponse.from(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = findRoleById(id);
        roleRepository.delete(role);
    }

    @Transactional
    public RoleResponse assignPermissionsToRole(Long roleId, RolePermissionAssignRequest request) {
        Role role = findRoleById(roleId);
        List<Permission> permissions =
                request.permissionIds().stream().map(this::findPermissionById).toList();

        role.addPermissions(permissions);
        return RoleResponse.from(role);
    }

    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = findRoleById(roleId);
        Permission permission = findPermissionById(permissionId);
        role.removePermission(permission);
    }

    private Role findRoleById(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    private Permission findPermissionById(Long id) {
        return permissionRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));
    }
}
