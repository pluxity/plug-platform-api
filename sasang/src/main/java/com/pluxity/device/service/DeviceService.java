package com.pluxity.device.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.device.dto.DeviceCreateRequest;
import com.pluxity.device.dto.DeviceResponse;
import com.pluxity.device.dto.DeviceUpdateRequest;
import com.pluxity.device.entity.DefaultDevice;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DefaultDeviceRepository;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.facility.entity.Station;
import com.pluxity.facility.repository.StationRepository;
import com.pluxity.feature.entity.Feature;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DefaultDeviceRepository repository;
    private final DeviceCategoryRepository categoryRepository;
    private final StationRepository stationRepository;
    private final AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public DeviceResponse findById(Long id) {
        DefaultDevice device = findDeviceById(id);
        return DeviceResponse.from(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAll() {
        return repository.findAll().stream()
                .map(DeviceResponse::from)
                .toList();
    }

    @Transactional
    public Long save(DeviceCreateRequest request) {
        DefaultDevice defaultDevice = createDefaultDevice(request);

        DefaultDevice saved = repository.save(defaultDevice);
        return saved.getId();
    }

    private DefaultDevice createDefaultDevice(DeviceCreateRequest request) {
        DeviceCategory category = request.deviceCategoryId() != null
                ? findCategoryById(request.deviceCategoryId())
                : null;

        Station station = request.stationId() != null
                ? findStationById(request.stationId())
                : null;

        Asset asset2d = request.asset2dId() != null
                ? findAssetById(request.asset3dId())
                : null;

        Asset asset3d = request.asset3dId() != null
                ? findAssetById(request.asset3dId())
                : null;

        Feature feature = Feature.create(request.feature());

        return DefaultDevice.create(
                feature,
                category,
                station,
                asset2d,
                asset3d,
                request.name(),
                request.code(),
                request.description()
        );
    }

    @Transactional
    public void update(Long id, DeviceUpdateRequest request) {
        DefaultDevice device = findDeviceById(id);

        DeviceCategory categoryToUpdate = null;
        if (request.deviceCategoryId() != null) {
            categoryToUpdate = findCategoryById(request.deviceCategoryId());
        }

        Station stationToUpdate = null;
        if (request.stationId() != null) {
            stationToUpdate = findStationById(request.stationId());
        }

        Asset asset2dToUpdate = null;
        if (request.asset2dId() != null) {
            asset2dToUpdate = findAssetById(request.asset2dId());
        }

        Asset asset3dToUpdate = null;
        if (request.asset3dId() != null) {
            asset3dToUpdate = findAssetById(request.asset3dId());
        }
        if (request.feature() != null) {
            device.getFeature().update(request.feature());
        }

        device.update(
                categoryToUpdate,
                stationToUpdate,
                asset2dToUpdate,
                asset3dToUpdate,
                request.name(),
                request.code(),
                request.description(),
                request.feature()
        );
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findDeviceById(id));
    }

    private DefaultDevice findDeviceById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND, "해당 디바이스를 찾을 수 없습니다 : " + id));
    }

    private DeviceCategory findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("DeviceCategory not found", HttpStatus.NOT_FOUND, "해당 디바이스 카테고리를 찾을 수 없습니다 : " + id));
    }

    private Station findStationById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Station not found", HttpStatus.NOT_FOUND, "해당 스테이션을 찾을 수 없습니다 : " + id));
    }

    private Asset findAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new CustomException("Asset not found", HttpStatus.NOT_FOUND, "해당 에셋을 찾을 수 없습니다 : " + id));
    }
}
