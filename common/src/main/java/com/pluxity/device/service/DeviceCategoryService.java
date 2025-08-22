package com.pluxity.device.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.category.service.CategoryService;
import com.pluxity.device.dto.*;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.device.repository.DeviceRepository;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.global.utils.SortUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceCategoryService extends CategoryService<DeviceCategory> {

    public static final String DEVICE_CATEGORIES = "device-categories/";
    private final DeviceCategoryRepository deviceCategoryRepository;
    private final DeviceRepository deviceRepository;
    private final FileService fileService;
    private final FacilityService facilityService;

    @Override
    protected JpaRepository<DeviceCategory, Long> getRepository() {
        return deviceCategoryRepository;
    }

    @Transactional
    public Long create(DeviceCategoryRequest request) {
        DeviceCategory deviceCategory =
                DeviceCategory.builder().name(request.name()).iconFileId(request.thumbnailFileId()).build();
        DeviceCategory parent = MappingUtils.findByIdIfExists(request.parentId(), super::findById);

        Long deviceCategoryId = super.create(deviceCategory, parent);

        if (request.thumbnailFileId() != null) {
            deviceCategory.updateIconFileId(request.thumbnailFileId());
            fileService.finalizeUpload(
                    request.thumbnailFileId(), DEVICE_CATEGORIES + deviceCategoryId + "/");
        }

        return deviceCategoryId;
    }

    @Transactional(readOnly = true)
    public DeviceCategoryResponse getDeviceCategory(Long id) {
        List<DeviceCategoryResponse> allCategories = getDeviceCategories();

        return findCategoryInTree(allCategories, id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_DEVICE_CATEGORY, id));
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
    public List<DeviceCategoryResponse> getDeviceCategories() {
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

        return MappingUtils.makeCategoryTree(
                list,
                DeviceCategoryResponse::id,
                DeviceCategoryResponse::parentId,
                DeviceCategoryResponse::children);
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
            throw new CustomException(CATEGORY_HAS_CHILDREN);
        }

        if (!deviceCategory.getDevices().isEmpty()) {
            throw new CustomException(CATEGORY_HAS_DEVICES);
        }

        deviceCategoryRepository.delete(deviceCategory);
    }

    @Transactional(readOnly = true)
    public List<DeviceCategoryResponse> getRootDeviceCategoryResponses() {
        return getRootCategories().stream()
                .map(this::createDeviceCategoryResponse)
                .collect(Collectors.toList());
    }

    protected DeviceCategoryResponse createDeviceCategoryResponse(DeviceCategory deviceCategory) {
        return DeviceCategoryResponse.from(
                deviceCategory,
                deviceCategory.getIconFileId() != null
                        ? fileService.getFileResponse(deviceCategory.getIconFileId())
                        : null);
    }

    @Transactional(readOnly = true)
    public List<DeviceInfoResponse> getDevicesByCategoryId(Long id, Long facilityId) {
        DeviceCategory category = findById(id);
        Facility facility = facilityService.findById(facilityId);
        List<Device> list = deviceRepository.findByCategoryAndFacility(category, facility);
        return list.stream().map(Device::toDeviceInfo).toList();
    }

    public DeviceCategoryDepthResponse getDeviceCategoryDepth() {
        return new DeviceCategoryDepthResponse(DeviceCategory.builder().build().getMaxDepth());
    }
}
