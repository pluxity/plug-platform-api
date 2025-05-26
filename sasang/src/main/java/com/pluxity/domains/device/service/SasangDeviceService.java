package com.pluxity.domains.device.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.domains.device.dto.SasangDeviceCreateRequest;
import com.pluxity.domains.device.dto.SasangDeviceResponse;
import com.pluxity.domains.device.dto.SasangDeviceUpdateRequest;
import com.pluxity.domains.device.entity.SasangDevice;
import com.pluxity.domains.device.repository.SasangDeviceRepository;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
import com.pluxity.feature.entity.Feature;
import com.pluxity.global.exception.CustomException;
import com.pluxity.icon.entity.Icon;
import com.pluxity.icon.repository.IconRepository;
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
    private final StationRepository stationRepository;
    private final AssetRepository assetRepository;
    private final IconRepository iconRepository;

    @Transactional(readOnly = true)
    public SasangDeviceResponse findById(Long id) {
        SasangDevice device = findDeviceById(id);
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

        Feature feature = Feature.create(request.feature());

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
        SasangDevice device = findDeviceById(id);

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
        repository.delete(findDeviceById(id));
    }

    private SasangDevice findDeviceById(Long id) {
        return repository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Device not found", HttpStatus.NOT_FOUND, "해당 디바이스를 찾을 수 없습니다 : " + id));
    }

    private DeviceCategory findCategoryById(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "DeviceCategory not found",
                                        HttpStatus.NOT_FOUND,
                                        "해당 디바이스 카테고리를 찾을 수 없습니다 : " + id));
    }

    private Station findStationById(Long id) {
        return stationRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Station not found", HttpStatus.NOT_FOUND, "해당 스테이션을 찾을 수 없습니다 : " + id));
    }

    private Asset findAssetById(Long id) {
        return assetRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Asset not found", HttpStatus.NOT_FOUND, "해당 에셋을 찾을 수 없습니다 : " + id));
    }

    private Icon findIconById(Long id) {
        return iconRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Icon not found", HttpStatus.NOT_FOUND, "해당 아이콘을 찾을 수 없습니다 : " + id));
    }
}
