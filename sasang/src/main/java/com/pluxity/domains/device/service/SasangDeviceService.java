package com.pluxity.domains.device.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.service.AssetService;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.domains.device.dto.SasangDeviceCreateRequest;
import com.pluxity.domains.device.dto.SasangDeviceResponse;
import com.pluxity.domains.device.dto.SasangDeviceUpdateRequest;
import com.pluxity.domains.device.entity.SasangDevice;
import com.pluxity.domains.device.repository.SasangDeviceRepository;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationService;
import com.pluxity.feature.entity.Feature;
import com.pluxity.global.exception.CustomException;
import com.pluxity.icon.entity.Icon;
import com.pluxity.icon.service.IconService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SasangDeviceService {

    private final SasangDeviceRepository repository;
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceCategoryService deviceCategoryService;
    private final StationService stationService;
    private final AssetService assetService;
    private final IconService iconService;

    @Transactional(readOnly = true)
    public SasangDeviceResponse findDeviceById(Long id) {
        SasangDevice device = findById(id);
        return SasangDeviceResponse.from(device);
    }

    @Transactional(readOnly = true)
    public List<SasangDeviceResponse> findAll() {
        return repository.findAll().stream().map(SasangDeviceResponse::from).toList();
    }

    @Transactional
    public Long save(SasangDeviceCreateRequest request) {
        SasangDevice sasangDevice = createSasangDevice(request);

        SasangDevice saved = repository.save(sasangDevice);
        return saved.getId();
    }

    private SasangDevice createSasangDevice(SasangDeviceCreateRequest request) {
        DeviceCategory category =
                request.deviceCategoryId() != null ? findCategoryById(request.deviceCategoryId()) : null;

        Station station = request.stationId() != null ? findStationById(request.stationId()) : null;

        Asset asset = request.asset() != null ? findAssetById(request.asset()) : null;

        Icon icon = request.iconId() != null ? findIconById(request.iconId()) : null;

        Feature feature = request.feature() != null ? Feature.create(request.feature()) : null;

        return SasangDevice.create(
                feature,
                category,
                station,
                icon,
                asset,
                request.name(),
                request.code(),
                request.description());
    }

    @Transactional
    public void update(Long id, SasangDeviceUpdateRequest request) {
        SasangDevice device = findById(id);

        DeviceCategory categoryToUpdate = null;
        if (request.deviceCategoryId() != null) {
            categoryToUpdate = findCategoryById(request.deviceCategoryId());
        }

        Station stationToUpdate = null;
        if (request.stationId() != null) {
            stationToUpdate = findStationById(request.stationId());
        }

        Icon icon = null;
        if (request.iconId() != null) {
            icon = findIconById(request.iconId());
        }

        Asset asset = null;
        if (request.asset() != null) {
            asset = findAssetById(request.asset());
        }

        if (request.feature() != null) {
            device.getFeature().update(request.feature());
        }

        device.update(
                categoryToUpdate,
                stationToUpdate,
                icon,
                asset,
                request.name(),
                request.code(),
                request.description(),
                request.feature());
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    @Transactional
    public SasangDevice findById(Long id) {
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

    private Station findStationById(Long id) {
        return stationService.findStationById(id);
    }

    private Asset findAssetById(Long id) {
        return assetService.findById(id);
    }

    private Icon findIconById(Long id) {
        return iconService.findById(id);
    }

    @Transactional
    public SasangDeviceResponse assignCategory(Long deviceId, Long categoryId) {
        SasangDevice device = findById(deviceId);
        DeviceCategory category = findCategoryById(categoryId);

        device.updateCategory(category);

        return SasangDeviceResponse.from(device);
    }

    @Transactional
    public SasangDeviceResponse removeCategory(Long deviceId) {
        SasangDevice device = findById(deviceId);

        if (device.getCategory() == null) {
            throw new CustomException(
                    "No Category Assigned",
                    HttpStatus.BAD_REQUEST,
                    String.format("DeviceCategory [%d]에 할당된 카테고리가 없습니다", deviceId));
        }

        device.updateCategory(null);

        return SasangDeviceResponse.from(device);
    }
}
