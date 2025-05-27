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
public class NfluxService {

    private final NfluxRepository repository;
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceCategoryService deviceCategoryService;
    private final StationService stationService;
    private final AssetService assetService;
    private final IconService iconService;

    @Transactional(readOnly = true)
    public NfluxResponse findDeviceById(Long id) {
        Nflux device = findById(id);
        return NfluxResponse.from(device);
    }

    @Transactional(readOnly = true)
    public List<NfluxResponse> findAll() {
        return repository.findAll().stream().map(NfluxResponse::from).toList();
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

        Station station = request.stationId() != null ? findStationById(request.stationId()) : null;

        Asset asset = request.asset() != null ? findAssetById(request.asset()) : null;

        Icon icon = request.iconId() != null ? findIconById(request.iconId()) : null;

        Feature feature = request.feature() != null ? Feature.create(request.feature()) : null;

        return Nflux.create(
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
    public void update(Long id, NfluxUpdateRequest request) {
        Nflux device = findById(id);

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
    public NfluxResponse assignCategory(Long deviceId, Long categoryId) {
        Nflux device = findById(deviceId);
        DeviceCategory category = findCategoryById(categoryId);

        device.updateCategory(category);

        return NfluxResponse.from(device);
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

        return NfluxResponse.from(device);
    }
}
