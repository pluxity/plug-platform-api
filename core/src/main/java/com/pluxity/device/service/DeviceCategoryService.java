package com.pluxity.device.service;

import com.pluxity.category.service.CategoryService;
import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.device.dto.DeviceCategoryTreeResponse;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.icon.entity.Icon;
import com.pluxity.icon.repository.IconRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceCategoryService extends CategoryService<DeviceCategory> {

    private final DeviceCategoryRepository deviceCategoryRepository;
    private final IconRepository iconRepository;

    @Override
    protected JpaRepository<DeviceCategory, Long> getRepository() {
        return deviceCategoryRepository;
    }

    @Transactional
    public Long create(DeviceCategoryRequest request) {
        DeviceCategory parent = null;
        if (request.getParentId() != null) {
            parent = findById(request.getParentId());
        }

        Icon icon = null;
        if (request.getIconId() != null) {
            icon = iconRepository.findById(request.getIconId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        }

        DeviceCategory deviceCategory = DeviceCategory.builder()
                .name(request.getName())
                .build();
        
        if (parent != null) {
            deviceCategory.assignToParent(parent);
        }
        
        if (icon != null) {
            deviceCategory.updateIcon(icon);
        }

        return deviceCategoryRepository.save(deviceCategory).getId();
    }

    @Transactional
    public void update(Long id, DeviceCategoryRequest request) {
        DeviceCategory deviceCategory = findById(id);
        
        if (request.getName() != null) {
            deviceCategory.setName(request.getName());
        }
        
        if (request.getIconId() != null) {
            Icon icon = iconRepository.findById(request.getIconId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
            deviceCategory.updateIcon(icon);
        }
        
        if (request.getParentId() != null) {
            DeviceCategory parent = findById(request.getParentId());
            deviceCategory.assignToParent(parent);
        }
    }

    @Transactional
    public void delete(Long id) {
        DeviceCategory deviceCategory = findById(id);
        
        if (!deviceCategory.getDevices().isEmpty()) {
            throw new CustomException(ErrorCode.CATEGORY_HAS_DEVICES);
        }
        
        deviceCategoryRepository.delete(deviceCategory);
    }

    public List<DeviceCategoryResponse> getRootDeviceCategoryResponses() {
        return getRootCategories().stream().map(DeviceCategoryResponse::from).collect(Collectors.toList());
    }
    
    public List<DeviceCategoryResponse> getChildrenResponses(Long id) {
        return getChildren(id).stream().map(DeviceCategoryResponse::from).collect(Collectors.toList());
    }

    public List<DeviceCategoryTreeResponse> getDeviceCategoryTree() {
        return getRootCategories().stream().map(DeviceCategoryTreeResponse::from).collect(Collectors.toList());
    }

    public DeviceCategoryResponse getDeviceCategoryResponse(Long id) {
        return DeviceCategoryResponse.from(findById(id));
    }
} 