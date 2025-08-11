package com.pluxity.device;

import com.pluxity.cctv.CctvService;
import com.pluxity.cctv.dto.CctvResponse;
import com.pluxity.cctv.entity.DeviceCctv;
import com.pluxity.cctv.repository.DeviceCctvRepository;
import com.pluxity.device.dto.*;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.annotation.CheckPermissionCategory;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.permission.ResourceType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private final FileService fileService;

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
        return createResponse(gsDevice, getThumbnailFile(gsDevice));
    }

    private FileResponse getThumbnailFile(Device gsDevice) {
        return Optional.ofNullable(gsDevice.getCategory())
                .map(DeviceCategory::getIconFileId)
                .map(fileService::getFileResponse)
                .orElse(null);
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
        List<DeviceCategory> categoryList =
                gsDevices.stream().map(Device::getCategory).filter(Objects::nonNull).toList();
        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(categoryList, v -> Stream.of(v.getIconFileId()), fileService);
        return gsDevices.stream()
                .map(
                        gsDevice ->
                                createResponse(
                                        gsDevice,
                                        gsDevice.getCategory() != null
                                                ? fileMap.get(gsDevice.getCategory().getIconFileId())
                                                : null))
                .collect(Collectors.toList());
    }

    private static GsDeviceResponse createResponse(GsDevice gsDevice, FileResponse thumbnailFile) {
        return GsDeviceResponse.builder()
                .id(gsDevice.getId())
                .name(gsDevice.getName())
                .feature(gsDevice.getFeature() != null ? FeatureResponse.from(gsDevice.getFeature()) : null)
                .deviceCategory(
                        gsDevice.getCategory() != null
                                ? DeviceCategoryResponseWithoutChildren.from(gsDevice.getCategory(), thumbnailFile)
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
        DeviceCategory category =
                MappingUtils.findByIdIfExists(request.categoryId(), deviceCategoryService::findById);
        device.changeCategory(category);
    }

    @Transactional
    public void delete(String id) {
        GsDevice device = getDevice(id);
        device.clearAllRelations();
        deviceCctvRepository.deleteByDevice(device);
        repository.delete(device);
    }

    @Transactional
    public void assignCategory(String deviceId, Long categoryId) {
        GsDevice device = getDevice(deviceId);
        DeviceCategory deviceCategory = deviceCategoryService.findById(categoryId);

        device.changeCategory(deviceCategory);
        log.info("디바이스 [{}}에 카테고리 [{}]가 할당되었습니다.", deviceId, categoryId);
    }

    @Transactional
    public void removeCategory(String deviceId) {
        GsDevice device = getDevice(deviceId);

        if (device.getCategory() == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_DEVICE_CATEGORY, deviceId);
        }

        device.changeCategory(null);
        log.info("디바이스 [{}]에서 카테고리가 제거되었습니다.", deviceId);
    }

    @Transactional(readOnly = true)
    public List<CctvResponse> getCctvByDeviceId(String deviceId) {
        GsDevice device = getDevice(deviceId);
        List<DeviceCctv> deviceCctvs = deviceCctvRepository.findByDevice(device);
        return deviceCctvs.stream()
                .map(DeviceCctv::getCctv)
                .map(
                        cctv ->
                                CctvResponse.builder()
                                        .id(cctv.getId())
                                        .name(cctv.getName())
                                        .url(cctv.getUrl())
                                        .feature(
                                                cctv.getFeature() != null ? FeatureResponse.from(cctv.getFeature()) : null)
                                        .deviceCategory(
                                                cctv.getCategory() != null
                                                        ? DeviceCategoryResponseWithoutChildren.from(
                                                                cctv.getCategory(), getThumbnailFile(cctv))
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
