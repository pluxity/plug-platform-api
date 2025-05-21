package com.pluxity.domains.acl.device_category.service;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.domains.acl.device_category.dto.DeviceCategoryResponseDto;
import com.pluxity.domains.acl.device_category.dto.PermissionRequestDto;
import com.pluxity.domains.acl.service.EntityAclOperations;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeviceCategoryAclService {

    private final DeviceCategoryRepository deviceCategoryRepository;
    private final EntityAclOperations entityAclOperations;
    private static final String ENTITY_TYPE = "DeviceCategory";
    private static final Class<?> ENTITY_CLASS = DeviceCategory.class;

    @Autowired
    public DeviceCategoryAclService(
            EntityAclOperations entityAclOperations, DeviceCategoryRepository deviceCategoryRepository) {
        this.deviceCategoryRepository = deviceCategoryRepository;
        this.entityAclOperations = entityAclOperations;
    }

    public void managePermission(PermissionRequestDto request) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Only admin users can manage permissions");
        }
        entityAclOperations.managePermission(request, ENTITY_TYPE, ENTITY_CLASS);
    }

    public DeviceCategory findById(Long id) {
        DeviceCategory category =
                deviceCategoryRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new AccessDeniedException("DeviceCategory not found with ID: " + id));
        if (!isAdmin() && !hasReadPermission(id)) {
            throw new AccessDeniedException("Access denied for device category with ID: " + id);
        }

        return category;
    }

    @Transactional(readOnly = true)
    public List<DeviceCategoryResponseDto> findAllAllowedForCurrentUser() {
        List<DeviceCategory> allCategories = deviceCategoryRepository.findAll();

        if (isAdmin()) {
            return allCategories.stream()
                    .map(this::convertToDto)
                    .collect(java.util.stream.Collectors.toList());
        }

        Function<DeviceCategory, DeviceCategoryResponseDto> dtoConverter = this::convertToDto;

        return entityAclOperations.findAllAllowedForCurrentUser(
                allCategories, DeviceCategory::getId, dtoConverter, ENTITY_CLASS);
    }

    private DeviceCategoryResponseDto convertToDto(DeviceCategory category) {
        return new DeviceCategoryResponseDto(category.getId(), category.getName());
    }

    public boolean hasReadPermission(Long categoryId) {
        if (isAdmin()) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return entityAclOperations.hasReadPermission(
                categoryId, ENTITY_CLASS, authentication.getName());
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
