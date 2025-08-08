package com.pluxity.device;

import com.pluxity.cctv.CctvService;
import com.pluxity.cctv.category.dto.CctvCategoryResponse;
import com.pluxity.cctv.dto.CctvResponse;
import com.pluxity.device.dto.*;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.entity.DeviceCctv;
import com.pluxity.device.repository.DeviceCctvRepository;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.global.annotation.CheckPermissionCategory;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.permission.ResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GsDeviceService {

    private final GsDeviceRepository repository;
    private final DeviceCategoryService deviceCategoryService;
    private final DeviceCctvRepository deviceCctvRepository;
    private final CctvService cctvService;

    @Transactional
    public String save(GsDeviceCreateRequest request) {
        DeviceCategory category =
                request.categoryId() != null ? deviceCategoryService.findById(request.categoryId()) : null;

        return repository.save(new GsDevice(request.id(), category, request.name())).getId();
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
        return gsDevices.stream().map(GsDeviceService::createResponse).collect(Collectors.toList());
    }

    private static GsDeviceResponse createResponse(GsDevice gsDevice) {
        return GsDeviceResponse.builder()
                .id(gsDevice.getId())
                .name(gsDevice.getName())
                .feature(gsDevice.getFeature() != null ? FeatureResponse.from(gsDevice.getFeature()) : null)
                .deviceCategory(
                        gsDevice.getCategory() != null
                                ? DeviceCategoryResponseWithoutChildren.from(gsDevice.getCategory())
                                : null)
                .build();
    }

    @Transactional
    public void update(String id, GsDeviceUpdateRequest request) {
        GsDevice device = getDevice(id);
        device.update(request.name());

        if (request.categoryId() != null) {
            DeviceCategory deviceCategory = deviceCategoryService.findById(request.categoryId());
            device.changeCategory(deviceCategory);
        }
    }

    @Transactional
    public void putUpdate(String id, GsDeviceUpdateRequest request) {
        GsDevice device = getDevice(id);
        device.putUpdate(request.name());

        if (request.categoryId() != null) {
            DeviceCategory deviceCategory = deviceCategoryService.findById(request.categoryId());
            device.updateCategory(deviceCategory);
        } else {
            device.updateCategory(null);
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

    @Transactional(readOnly = true)
    public List<CctvResponse> getCctvByDeviceId(String deviceId) {
        GsDevice device = getDevice(deviceId);
        List<DeviceCctv> deviceCctvs = deviceCctvRepository.findByDevice(device);
        return deviceCctvs.stream()
                .map(
                        deviceCctv ->
                                CctvResponse.builder()
                                        .id(deviceCctv.getCctv().getId())
                                        .name(deviceCctv.getCctv().getName())
                                        .url(deviceCctv.getCctv().getUrl())
                                        .feature(
                                                deviceCctv.getCctv().getFeature() != null
                                                        ? FeatureResponse.from(deviceCctv.getCctv().getFeature())
                                                        : null)
                                        .cctvCategory(
                                                deviceCctv.getCctv().getCategory() != null
                                                        ? CctvCategoryResponse.from(deviceCctv.getCctv().getCategory())
                                                        : null)
                                        .build())
                .toList();
    }

    @Transactional
    public void assignCctvToDevice(String deviceId, GsDeviceCctvUpdateRequest request) {
        GsDevice device = getDevice(deviceId);
        List<String> existIds =
                deviceCctvRepository.findByDevice(device).stream().map(v -> v.getCctv().getId()).toList();

        List<String> requestIds = new ArrayList<>(request.cctvIds());

        // 추가할 cctv id
        List<DeviceCctv> saveList =
                requestIds.stream()
                        .filter(id -> !existIds.contains(id))
                        .map(
                                cctvId ->
                                        DeviceCctv.builder().cctv(cctvService.findById(cctvId)).device(device).build())
                        .toList();

        // 삭제할 cctv id
        List<String> removeList = existIds.stream().filter(id -> !requestIds.contains(id)).toList();

        // 추가
        if (!saveList.isEmpty()) {
            deviceCctvRepository.saveAll(saveList);
        }

        // 삭제
        if (!removeList.isEmpty()) {
            deviceCctvRepository.deleteByCctvIdIn(removeList);
        }
    }
}
