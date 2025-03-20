package com.pluxity.building.service;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingListResponse;
import com.pluxity.building.entity.Building;
import com.pluxity.building.entity.BuildingPermission;
import com.pluxity.building.repository.BuildingPermissionRepository;
import com.pluxity.building.repository.BuildingRepository;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.entity.UserRole;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pluxity.global.constant.ErrorCode.FAILED_TO_UPLOAD_FILE;
import static com.pluxity.global.constant.ErrorCode.PERMISSION_DENIED;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final FileService fileService;
    private final BuildingRepository repository;
    private final BuildingPermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long save(BuildingCreateRequest dto) {

        var building = Building.builder()
                                .name(dto.name())
                                .build();

        var savedBuilding = repository.save(building);

        try {
            FileEntity file = fileService.finalizeUpload(dto.fileId(), "drawings/" + savedBuilding.getId() + "/");
            building.setDrawing(file);
        } catch (CustomException e) {
            log.error("파일 업로드 실패: {}", savedBuilding.getId(), e);
            throw new CustomException(FAILED_TO_UPLOAD_FILE, "파일 업로드에 실패했습니다");
        }

        return savedBuilding.getId();
    }
    
    @Transactional(readOnly = true)
    public Building findById(Long id, Long userId) {
        Building building = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("빌딩을 찾을 수 없습니다: " + id));
        
        // 권한 확인
        if (!hasPermission(userId, id)) {
            throw new CustomException(PERMISSION_DENIED, "빌딩에 접근할 권한이 없습니다");
        }
        
        return building;
    }
    
    @Transactional(readOnly = true)
    public List<BuildingListResponse> findAll(Long userId) {
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
            // ADMIN 역할이 있으면 모든 빌딩 조회
            return repository.findAll().stream()
                    .map(this::mapToBuildingListResponse)
                    .collect(Collectors.toList());
        } else {
            // 사용자의 역할에 연결된 모든 BuildingPermission 조회
            Set<Long> accessibleBuildingIds = new HashSet<>();
            
            for (Role role : userRoles) {
                List<BuildingPermission> permissions = permissionRepository.findByRole(role);
                
                // 각 Permission에서 resourceId(buildingId) 추출
                permissions.stream()
                        .map(BuildingPermission::getResourceId)
                        .forEach(accessibleBuildingIds::add);
            }
            
            // 접근 가능한 빌딩만 조회
            return repository.findAllById(accessibleBuildingIds).stream()
                    .map(this::mapToBuildingListResponse)
                    .collect(Collectors.toList());
        }
    }
    
    private BuildingListResponse mapToBuildingListResponse(Building building) {
        return BuildingListResponse.builder()
                .id(building.getId())
                .name(building.getName())
                .build();
    }
    
    private boolean hasPermission(Long userId, Long buildingId) {
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
            List<BuildingPermission> permissions = permissionRepository.findByRoleAndBuildingId(role, buildingId);
            if (!permissions.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
}
