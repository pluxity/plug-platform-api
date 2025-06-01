package com.pluxity.domains.device.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.service.AssetService;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.domains.device.dto.NfluxCategoryGroupResponse;
import com.pluxity.domains.device.dto.NfluxCreateRequest;
import com.pluxity.domains.device.dto.NfluxResponse;
import com.pluxity.domains.device.dto.NfluxUpdateRequest;
import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.domains.device.repository.NfluxRepository;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final StationRepository stationRepository;
    private final FileService fileService;

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
        Nflux device = findById(id);

        // 디바이스 삭제 전 연관관계 정리 로깅
        log.info("Nflux 디바이스 [{}] 삭제 전 연관관계 정리 시작", id);

        // 모든 연관관계 제거 (Feature, DeviceCategory)
        device.clearAllRelations();

        log.info("Nflux 디바이스 [{}]의 모든 연관관계 제거 완료, 삭제 진행", id);
        repository.delete(device);
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

    @Transactional(readOnly = true)
    public List<NfluxCategoryGroupResponse> findByStationIdGroupByCategory(Long stationId) {
        Station station =
                stationRepository
                        .findById(stationId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Station not found",
                                                HttpStatus.NOT_FOUND,
                                                "해당 스테이션을 찾을 수 없습니다: " + stationId));

        // 스테이션 ID와 관련된 Nflux 디바이스 찾기
        List<Nflux> devices =
                repository.findAll().stream()
                        .filter(
                                nflux ->
                                        nflux.getFeature() != null
                                                && nflux.getFeature().getFacility() != null
                                                && nflux.getFeature().getFacility() instanceof Station
                                                && nflux.getFeature().getFacility().getId().equals(stationId))
                        .toList();

        // 카테고리별로 그룹화
        Map<DeviceCategory, List<Nflux>> devicesByCategory =
                devices.stream()
                        .filter(nflux -> nflux.getCategory() != null)
                        .collect(Collectors.groupingBy(Nflux::getCategory));

        // 응답 객체 생성
        return devicesByCategory.entrySet().stream()
                .map(
                        entry -> {
                            DeviceCategory category = entry.getKey();
                            List<Nflux> categoryDevices = entry.getValue();
                            String contextPath = null;
                            FileResponse iconFile = null;

                            // NfluxCategory인 경우 contextPath 가져오기
                            if (category instanceof NfluxCategory) {
                                contextPath = ((NfluxCategory) category).getContextPath();
                            }

                            // 카테고리의 아이콘 파일 ID가 있으면 FileResponse 생성
                            if (category.getIconFileId() != null) {
                                iconFile = fileService.getFileResponse(category.getIconFileId());
                            }

                            return new NfluxCategoryGroupResponse(
                                    category.getId(),
                                    category.getName(),
                                    contextPath,
                                    iconFile,
                                    categoryDevices.stream().map(this::createResponse).toList());
                        })
                .toList();
    }
}
