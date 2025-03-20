package com.pluxity.permission.service;

import com.pluxity.permission.entity.Permission;
import com.pluxity.permission.repository.BasePermissionRepository;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final BasePermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<Permission> findByRole(Long roleId) {
        Role role = findRoleById(roleId);
        return permissionRepository.findByRole(role);
    }
    
    @Transactional(readOnly = true)
    public List<Permission> findByUsername(String username) {
        return permissionRepository.findByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public <T extends Permission> List<T> findByUsernameAndType(String username, Class<T> type) {
        return permissionRepository.findByUsernameAndType(username, type);
    }
    
    @Transactional(readOnly = true)
    public boolean hasPermission(String username, Long resourceId) {
        // ADMIN 역할이 있는지 확인
        if (permissionRepository.isAdmin(username)) {
            return true;
        }
        
        // 특정 리소스에 대한 권한이 있는지 확인
        return permissionRepository.existsByUsernameAndResourceId(username, resourceId);
    }

    @Transactional
    public void delete(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("권한을 찾을 수 없습니다: " + permissionId));
        
        permissionRepository.delete(permission);
    }
    
    private Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("역할을 찾을 수 없습니다: " + roleId));
    }
} 