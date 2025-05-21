package com.pluxity.domains.acl.device.service;

import com.pluxity.domains.acl.service.EntityAclOperations;
import com.pluxity.domains.device.dto.DeviceResponse;
import com.pluxity.domains.device.entity.DefaultDevice;
import com.pluxity.domains.device.repository.DefaultDeviceRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultDeviceAclService {

    private final DefaultDeviceRepository defaultDeviceRepository;
    private final EntityAclOperations entityAclOperations;
    private static final String ENTITY_TYPE = "DefaultDevice";
    private static final Class<?> ENTITY_CLASS = DefaultDevice.class;

    @Autowired
    public DefaultDeviceAclService(
            EntityAclOperations entityAclOperations, DefaultDeviceRepository defaultDeviceRepository) {
        this.defaultDeviceRepository = defaultDeviceRepository;
        this.entityAclOperations = entityAclOperations;
    }

    @Transactional(readOnly = true)
    public DefaultDevice findById(Long id) {
        DefaultDevice device =
                defaultDeviceRepository
                        .findById(id)
                        .orElseThrow(() -> new AccessDeniedException("DefaultDevice not found with ID: " + id));

        // 디바이스에 카테고리가 있는 경우 권한 확인
        if (device.getCategory() != null) {
            Long categoryId = device.getCategory().getId();

            // 관리자이거나 해당 카테고리에 READ 권한이 있는지 확인
            if (!isAdmin() && !hasReadPermissionForCategory(categoryId)) {
                throw new AccessDeniedException("Access denied for device with category ID: " + categoryId);
            }
        }

        return device;
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAllAllowedForCurrentUser() {
        List<DefaultDevice> allDevices = defaultDeviceRepository.findAll();

        // 관리자인 경우 모든 디바이스 반환
        if (isAdmin()) {
            return allDevices.stream()
                    .filter(device -> device.getCategory() != null) // 카테고리가 있는 디바이스만 포함
                    .map(DeviceResponse::from)
                    .collect(Collectors.toList());
        }

        // 카테고리 접근 권한이 있는 디바이스만 필터링
        return allDevices.stream()
                .filter(this::hasReadPermissionForDeviceCategory)
                .map(DeviceResponse::from)
                .collect(Collectors.toList());
    }

    private boolean hasReadPermissionForDeviceCategory(DefaultDevice device) {
        // 카테고리가 없는 경우는 보이지 않도록 처리
        if (device.getCategory() == null) {
            return false;
        }

        return hasReadPermissionForCategory(device.getCategory().getId());
    }

    private boolean hasReadPermissionForCategory(Long categoryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return entityAclOperations.hasReadPermission(
                categoryId, com.pluxity.device.entity.DeviceCategory.class, authentication.getName());
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }
}
