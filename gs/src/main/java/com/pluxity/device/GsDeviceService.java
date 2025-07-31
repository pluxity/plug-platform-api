package com.pluxity.device;

import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.device.dto.GsDeviceCreateRequest;
import com.pluxity.device.dto.GsDeviceResponse;
import com.pluxity.device.dto.GsDeviceUpdateRequest;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.global.annotation.CheckPermissionCategory;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.permission.ResourceType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GsDeviceService {

    private final GsDeviceRepository repository;
    private final FeatureService featureService;
    private final DeviceCategoryService deviceCategoryService;

    @Transactional
    public String save(GsDeviceCreateRequest request) {
        GsDevice gsDevice = createGsDevice(request);
        return repository.save(gsDevice).getId();
    }

    private GsDevice createGsDevice(GsDeviceCreateRequest request) {
        DeviceCategory category =
                request.categoryId() != null ? deviceCategoryService.findById(request.categoryId()) : null;

        Feature feature =
                request.featureId() != null ? featureService.findFeatureById(request.featureId()) : null;

        return new GsDevice(request.id(), feature, category, request.name());
    }

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.DEVICE_CATEGORY)
    public GsDeviceResponse findById(String id) {
        GsDevice gsDevice = getDevice(id);
        return createResponse(gsDevice);
    }

    private GsDevice getDevice(String id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DEVICE, id));
    }

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.DEVICE_CATEGORY)
    public List<GsDeviceResponse> findAll() {
        List<GsDevice> gsDevices = repository.findAll();
        return gsDevices.stream().map(GsDeviceService::createResponse).toList();
    }

    private static GsDeviceResponse createResponse(GsDevice gsDevice) {
        return GsDeviceResponse.builder()
                .id(gsDevice.getId())
                .name(gsDevice.getName())
                .feature(gsDevice.getFeature() != null ? FeatureResponse.from(gsDevice.getFeature()) : null)
                .deviceCategory(
                        gsDevice.getCategory() != null
                                ? DeviceCategoryResponse.from(gsDevice.getCategory())
                                : null)
                .build();
    }

    @Transactional
    public void update(String id, GsDeviceUpdateRequest request) {
        GsDevice device = getDevice(id);
        device.update(request.name());

        if (request.featureId() != null) {
            Feature feature = featureService.findFeatureById(request.featureId());
            device.changeFeature(feature);
        }
        if (request.categoryId() != null) {
            DeviceCategory deviceCategory = deviceCategoryService.findById(request.categoryId());
            device.changeCategory(deviceCategory);
        }
    }

    @Transactional
    public void delete(String id) {
        GsDevice device = getDevice(id);
        device.clearAllRelations();
        repository.delete(device);
    }

    @Transactional
    public void assignCategory(String deviceId, Long categoryId) {
        GsDevice device = getDevice(deviceId);
        DeviceCategory deviceCategory = deviceCategoryService.findById(categoryId);

        device.updateCategory(deviceCategory);
        log.info("디바이스 [{}}에 카테고리 [{}]가 할당되었습니다.", deviceId, categoryId);
    }

    @Transactional
    public void removeCategory(String deviceId) {
        GsDevice device = getDevice(deviceId);

        if (device.getCategory() == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_DEVICE_CATEGORY, deviceId);
        }

        device.updateCategory(null);
        log.info("디바이스 [{}]에서 카테고리가 제거되었습니다.", deviceId);
    }
}
