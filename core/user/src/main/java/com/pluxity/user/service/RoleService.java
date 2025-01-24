package com.pluxity.user.service;

import com.pluxity.user.dto.RequestRole;
import com.pluxity.user.dto.RequestRolePermissions;
import com.pluxity.user.dto.ResponsePermission;
import com.pluxity.user.dto.ResponseRole;
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
    public ResponseRole findById(Long id) {
        Role role = findRoleById(id);
        return ResponseRole.from(role);
    }

    @Transactional(readOnly = true)
    public List<ResponseRole> findAll() {
        return roleRepository.findAll().stream()
                .map(ResponseRole::from)
                .toList();
    }

    @Transactional
    public ResponseRole save(RequestRole request) {
        Role role = Role.builder()
                .roleName(request.roleName())
                .build();
        
        Role savedRole = roleRepository.save(role);
        return ResponseRole.from(savedRole);
    }

    @Transactional
    public ResponseRole update(Long id, RequestRole request) {
        Role role = findRoleById(id);
        role.updateRoleName(request.roleName());
        return ResponseRole.from(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = findRoleById(id);
        roleRepository.delete(role);
    }

    @Transactional
    public ResponseRole assignPermissionsToRole(Long roleId, RequestRolePermissions request) {
        Role role = findRoleById(roleId);
        List<Permission> permissions = request.permissionIds().stream()
                .map(this::findPermissionById)
                .toList();
        
        try {
            role.addPermissions(permissions);
            return ResponseRole.from(role);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to assign permissions: " + e.getMessage());
        }
    }

    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = findRoleById(roleId);
        Permission permission = findPermissionById(permissionId);
        
        try {
            role.removePermission(permission);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to remove permission: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ResponsePermission> getRolePermissions(Long roleId) {
        Role role = findRoleById(roleId);
        return role.getPermissions().stream()
                .map(ResponsePermission::from)
                .toList();
    }

    private Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    private Permission findPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));
    }
} 