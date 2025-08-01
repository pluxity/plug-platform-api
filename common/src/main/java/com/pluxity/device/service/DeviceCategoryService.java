package com.pluxity.device.service;

import com.pluxity.category.service.CategoryService;
import com.pluxity.device.dto.DeviceCategoryAllResponse;
import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.global.utils.SortUtils;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceCategoryService extends CategoryService<DeviceCategory> {

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

        if (request.getThumbnailFileId() != null) {
            deviceCategory.updateIconFileId(request.getThumbnailFileId());
        }

        return deviceCategoryRepository.save(deviceCategory).getId();
    }

    @Transactional(readOnly = true)
    public DeviceCategoryResponse getDeviceCategory(Long id) {
        DeviceCategoryAllResponse allCategories = getDeviceCategories();
        return allCategories.list().stream()
                .filter(v -> v.id().equals(id))
                .findFirst()
                .orElseThrow(notFoundDeviceCategory(id));
    }

    @Transactional(readOnly = true)
    public DeviceCategoryAllResponse getDeviceCategories() {
        List<DeviceCategory> allCategories =
                deviceCategoryRepository.findAll(SortUtils.getOrderByCreatedAtDesc());

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

    private Supplier<CustomException> notFoundDeviceCategory(Long id) {
        return () -> new CustomException(ErrorCode.NOT_FOUND_DEVICE_CATEGORY, id);
    }

    @Transactional
    public void update(Long id, DeviceCategoryRequest request) {
        DeviceCategory deviceCategory = findById(id);

        if (request.getName() != null) {
            deviceCategory.setName(request.getName());
        }

        if (request.getThumbnailFileId() != null
                && !request.getThumbnailFileId().equals(deviceCategory.getIconFileId())) {
            deviceCategory.updateIconFileId(request.getThumbnailFileId());
        }

        if (request.getParentId() != null) {
            DeviceCategory parent = findById(request.getParentId());
            deviceCategory.assignToParent(parent);
        }
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
    public List<DeviceCategoryResponse> getRootDeviceCategoryResponses() {
        return getRootCategories().stream()
                .map(this::createDeviceCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeviceCategoryResponse getDeviceCategoryResponse(Long id) {
        return createDeviceCategoryResponse(findById(id));
    }

    protected DeviceCategoryResponse createDeviceCategoryResponse(DeviceCategory deviceCategory) {
        return DeviceCategoryResponse.from(
                deviceCategory,
                deviceCategory.getIconFileId() != null
                        ? fileService.getFileResponse(deviceCategory.getIconFileId())
                        : null);
    }
}
