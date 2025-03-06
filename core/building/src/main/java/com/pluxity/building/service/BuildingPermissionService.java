package com.pluxity.building.service;

import com.pluxity.building.dto.BuildingPermissionCreateRequest;
import com.pluxity.building.dto.BuildingPermissionResponse;
import com.pluxity.building.entity.BuildingPermission;
import com.pluxity.building.repository.BuildingPermissionRepository;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.entity.UserRole;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingPermissionService {

    private final BuildingPermissionRepository buildingPermissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BuildingPermissionResponse> findByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        // 사용자의 모든 역할 가져오기
        Set<Role> userRoles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
        
        // 모든 역할에 대한 BuildingPermission 조회
        return userRoles.stream()
                .flatMap(role -> buildingPermissionRepository.findByRole(role).stream())
                .map(BuildingPermissionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long buildingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        // 사용자의 모든 역할 가져오기
        Set<Role> userRoles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
        
        // ADMIN 역할이 있는지 확인
        boolean isAdmin = userRoles.stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleName()));
        
        if (isAdmin) {
            // ADMIN 역할이 있으면 모든 빌딩에 접근 가능
            return true;
        }
        
        // 사용자의 역할에 연결된 BuildingPermission 중 해당 빌딩에 접근 가능한지 확인
        for (Role role : userRoles) {
            List<BuildingPermission> permissions = buildingPermissionRepository.findByRoleAndBuildingId(role, buildingId);
            if (!permissions.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }

    @Transactional
    public BuildingPermissionResponse create(BuildingPermissionCreateRequest request) {
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new EntityNotFoundException("역할을 찾을 수 없습니다: " + request.roleId()));
        
        // 이미 존재하는 권한인지 확인
        buildingPermissionRepository.findByRoleAndResourceId(role, request.buildingId())
                .ifPresent(p -> {
                    throw new IllegalStateException("이미 해당 빌딩에 대한 권한이 존재합니다.");
                });
        
        BuildingPermission permission = BuildingPermission.builder()
                .role(role)
                .buildingId(request.buildingId())
                .build();
        
        BuildingPermission savedPermission = buildingPermissionRepository.save(permission);
        return BuildingPermissionResponse.from(savedPermission);
    }

    @Transactional
    public void delete(Long permissionId) {
        BuildingPermission permission = buildingPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("권한을 찾을 수 없습니다: " + permissionId));
        
        buildingPermissionRepository.delete(permission);
    }
    
    private Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("역할을 찾을 수 없습니다: " + roleId));
    }
} 