package com.pluxity.domains.device.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.service.AssetService;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.domains.device.dto.NfluxCreateRequest;
import com.pluxity.domains.device.dto.NfluxResponse;
import com.pluxity.domains.device.dto.NfluxUpdateRequest;
import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.domains.device.repository.NfluxRepository;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NfluxService {

    private final NfluxRepository repository;
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceCategoryService deviceCategoryService;
    private final AssetService assetService;
    private final FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public NfluxResponse findDeviceById(Long id) {
        Nflux device = findById(id);
        return createResponse(device);
    }

    @Transactional(readOnly = true)
    public List<NfluxResponse> findAll() {
        return repository.findAll().stream().map(this::createResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NfluxResponse> findByCategoryId(Long categoryId) {
        return repository.findByCategoryId(categoryId).stream().map(this::createResponse).toList();
    }

    private NfluxResponse createResponse(Nflux device) {
        return new NfluxResponse(
                device.getId(),
                device.getFeature() != null ? FeatureResponse.from(device.getFeature()) : null,
                device.getCategory() != null ? device.getCategory().getId() : null,
                device.getCategory() != null ? device.getCategory().getName() : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getId()
                        : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getName()
                        : null,
                device.getName(),
                device.getCode(),
                device.getDescription(),
                BaseResponse.of(device));
    }

    @Transactional
    public Long save(NfluxCreateRequest request) {
        Nflux nflux = createSasangDevice(request);

        Nflux saved = repository.save(nflux);
        return saved.getId();
    }

    private Nflux createSasangDevice(NfluxCreateRequest request) {
        DeviceCategory category =
                request.deviceCategoryId() != null ? findCategoryById(request.deviceCategoryId()) : null;

        Asset asset = request.asset() != null ? findAssetById(request.asset()) : null;

        return Nflux.create(category, asset, request.name(), request.code(), request.description());
    }

    @Transactional
    public void update(Long id, NfluxUpdateRequest request) {
        Nflux device = findById(id);

        DeviceCategory categoryToUpdate = null;
        if (request.deviceCategoryId() != null) {
            categoryToUpdate = findCategoryById(request.deviceCategoryId());
        }

        Asset asset = null;
        if (request.asset() != null) {
            asset = findAssetById(request.asset());
        }

        device.update(categoryToUpdate, asset, request.name(), request.code(), request.description());
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    @Transactional
    public Nflux findById(Long id) {
        return repository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Device not found", HttpStatus.NOT_FOUND, "해당 디바이스를 찾을 수 없습니다 : " + id));
    }

    private DeviceCategory findCategoryById(Long id) {
        return deviceCategoryService.findById(id);
    }

    private Asset findAssetById(Long id) {
        return assetService.findById(id);
    }

    @Transactional
    public NfluxResponse assignCategory(Long deviceId, Long categoryId) {
        Nflux device = findById(deviceId);
        DeviceCategory category = findCategoryById(categoryId);

        device.updateCategory(category);

        return createResponse(device);
    }

    @Transactional
    public NfluxResponse removeCategory(Long deviceId) {
        Nflux device = findById(deviceId);

        if (device.getCategory() == null) {
            throw new CustomException(
                    "No Category Assigned",
                    HttpStatus.BAD_REQUEST,
                    String.format("DeviceCategory [%d]에 할당된 카테고리가 없습니다", deviceId));
        }

        device.updateCategory(null);

        return createResponse(device);
    }

    @Transactional
    public NfluxResponse assignFeatureToNflux(Long deviceId, String featureId) {
        Nflux device = findById(deviceId);
        Feature feature =
                featureRepository
                        .findById(featureId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Feature not found",
                                                HttpStatus.NOT_FOUND,
                                                "해당 피처를 찾을 수 없습니다: " + featureId));

        if (feature.getDevice() != null && !feature.getDevice().getId().equals(deviceId)) {
            throw new CustomException(
                    "Feature already assigned", HttpStatus.BAD_REQUEST, "해당 피처는 이미 다른 디바이스에 할당되어 있습니다.");
        }

        device.changeFeature(feature);
        return createResponse(device);
    }

    @Transactional
    public NfluxResponse removeFeatureFromNflux(Long deviceId) {
        Nflux device = findById(deviceId);

        if (device.getFeature() == null) {
            throw new CustomException(
                    "No Feature Assigned",
                    HttpStatus.BAD_REQUEST,
                    String.format("디바이스 ID [%d]에 할당된 피처가 없습니다", deviceId));
        }

        device.changeFeature(null);
        return createResponse(device);
    }
}
