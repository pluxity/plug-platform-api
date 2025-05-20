package com.pluxity.domains.device_category_acl.device.service;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.domains.acl.service.EntityAclOperations;
import com.pluxity.domains.device_category_acl.device.dto.DeviceCategoryResponseDto;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @PreAuthorize("hasRole('ADMIN')")
    public void managePermission(PermissionRequestDto request) {
        entityAclOperations.managePermission(request, ENTITY_TYPE, ENTITY_CLASS);
    }

    @PreAuthorize("hasPermission(#id, 'com.pluxity.device.entity.DeviceCategory', 'READ')")
    public DeviceCategory findById(Long id) {
        return deviceCategoryRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DeviceCategory not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DeviceCategoryResponseDto> findAllAllowedForCurrentUser() {
        List<DeviceCategory> allCategories = deviceCategoryRepository.findAll();

        Function<DeviceCategory, DeviceCategoryResponseDto> dtoConverter = this::convertToDto;

        return entityAclOperations.findAllAllowedForCurrentUser(
                allCategories, DeviceCategory::getId, dtoConverter, ENTITY_CLASS);
    }

    private DeviceCategoryResponseDto convertToDto(DeviceCategory category) {
        return new DeviceCategoryResponseDto(category.getId(), category.getName());
    }

    public boolean hasReadPermission(Long categoryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return entityAclOperations.hasReadPermission(
                categoryId, ENTITY_CLASS, authentication.getName());
    }
}
