package com.pluxity.device.service;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_DEVICE_CATEGORY;

import com.pluxity.category.service.CategoryService;
import com.pluxity.device.dto.*;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.utils.MappingUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceCategoryService extends CategoryService<DeviceCategory> {

    public static final String DEVICE_CATEGORIES = "device-categories/";
    private final DeviceCategoryRepository deviceCategoryRepository;
    private final FileService fileService;

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

        DeviceCategory deviceCategory =
                DeviceCategory.builder().name(request.getName()).parent(parent).build();

        Long id = deviceCategoryRepository.save(deviceCategory).getId();

        if (request.getIconFileId() != null) {
            deviceCategory.updateIconFileId(request.getIconFileId());
            fileService.finalizeUpload(request.getIconFileId(), DEVICE_CATEGORIES + id + "/");
        }

        return id;
    }

    @Transactional
    public void update(Long id, DeviceCategoryUpdateRequest request) {
        DeviceCategory deviceCategory = findById(id);

        if (request.parentId() == null) {
            if (request.name() != null) {
                deviceCategory.updateName(request.name());
            }
            deviceCategory.assignToRootPreservingEntity();
        } else {
            super.update(id, request.name(), request.parentId());
        }

        if (request.thumbnailFileId() != null) {
            deviceCategory.updateIconFileId(request.thumbnailFileId());
            fileService.finalizeUpload(
                    request.thumbnailFileId(), DEVICE_CATEGORIES + deviceCategory.getId() + "/");
        }

        deviceCategory.updateIconFileId(request.thumbnailFileId());
    }

    @Transactional
    public void delete(Long id) {
        DeviceCategory deviceCategory = findById(id);

        if (!deviceCategory.getChildren().isEmpty()) {
            throw new CustomException(ErrorCode.CATEGORY_HAS_DEVICES);
        }

        deviceCategoryRepository.delete(deviceCategory);
    }

    @Transactional(readOnly = true)
    public DeviceCategoryResponse getDeviceCategory(Long id) {
        DeviceCategoryAllResponse allCategories = getDeviceCategories();

        return findCategoryInTree(allCategories.list(), id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_DEVICE_CATEGORY, String.valueOf(id)));
    }

    private Optional<DeviceCategoryResponse> findCategoryInTree(
            List<DeviceCategoryResponse> categories, Long id) {
        for (DeviceCategoryResponse category : categories) {
            if (category.id().equals(id)) {
                return Optional.of(category);
            }

            if (category.children() != null && !category.children().isEmpty()) {
                Optional<DeviceCategoryResponse> foundInChildren =
                        findCategoryInTree(category.children(), id);
                if (foundInChildren.isPresent()) {
                    return foundInChildren;
                }
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public DeviceCategoryAllResponse getDeviceCategories() {
        List<DeviceCategory> allCategories =
                deviceCategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "CreatedAt"));

        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(
                        allCategories,
                        deviceCategory -> Stream.of(deviceCategory.getIconFileId()),
                        fileService);

        List<DeviceCategoryResponse> list =
                allCategories.stream()
                        .map(
                                category ->
                                        DeviceCategoryResponse.from(category, fileMap.get(category.getIconFileId())))
                        .toList();

        return DeviceCategoryAllResponse.of(
                DeviceCategory.builder().build().getMaxDepth(),
                MappingUtils.makeCategoryTree(
                        list,
                        DeviceCategoryResponse::id,
                        DeviceCategoryResponse::parentId,
                        DeviceCategoryResponse::children));
    }

    @Transactional(readOnly = true)
    public List<DeviceCategoryResponse> getChildDeviceCategories(Long parentId) {
        List<DeviceCategory> childCategories = deviceCategoryRepository.findByParentId(parentId);
        return childCategories.stream()
                .map(this::createDeviceCategoryResponseWithoutChildren)
                .collect(Collectors.toList());
    }

    private DeviceCategoryResponse createDeviceCategoryResponseWithoutChildren(
            DeviceCategory category) {
        FileResponse iconFile =
                category.getIconFileId() != null
                        ? fileService.getFileResponse(category.getIconFileId())
                        : null;
        return DeviceCategoryResponse.fromWithoutChildren(category, iconFile);
    }

    protected DeviceCategoryResponse createDeviceCategoryResponse(DeviceCategory deviceCategory) {
        return DeviceCategoryResponse.from(
                deviceCategory,
                deviceCategory.getIconFileId() != null
                        ? fileService.getFileResponse(deviceCategory.getIconFileId())
                        : null);
    }

    protected DeviceCategoryTreeResponse createDeviceCategoryTreeResponse(
            DeviceCategory deviceCategory) {
        return DeviceCategoryTreeResponse.from(
                deviceCategory,
                deviceCategory.getIconFileId() != null
                        ? fileService.getFileResponse(deviceCategory.getIconFileId())
                        : null);
    }
}
