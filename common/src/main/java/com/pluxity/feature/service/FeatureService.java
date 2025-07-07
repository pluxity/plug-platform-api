package com.pluxity.feature.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.asset.service.AssetService;
import com.pluxity.device.entity.Device;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityRepository;
import com.pluxity.facility.FacilityService;
import com.pluxity.feature.dto.FeatureAssignDto;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final FeatureRepository featureRepository;
    private final AssetRepository assetRepository;
    private final FacilityRepository facilityRepository;
    private final FacilityService facilityService;
    private final AssetService assetService;
    private final FileService fileService;
    @PersistenceContext private EntityManager entityManager;

    @Transactional
    public FeatureResponse createFeature(FeatureCreateRequest request) {
        log.debug(
                "피처 생성 요청: id={}, facilityId={}, assetId={}",
                request.id(),
                request.facilityId(),
                request.assetId());

        // ID 중복 체크
        String featureId = request.id();
        Optional<Feature> existingFeature = featureRepository.findById(featureId);
        if (existingFeature.isPresent()) {
            throw new CustomException(
                    "Feature already exists", HttpStatus.CONFLICT, "이미 존재하는 피처 ID입니다: " + featureId);
        }

        // 먼저 관련 엔티티 조회
        Facility facility = facilityService.findById(request.facilityId());
        Asset asset = assetService.findById(request.assetId());

        // Feature 엔티티 생성
        Feature feature = Feature.create(request, featureId);

        // 양방향 연관관계 설정 - 엔티티의 편의 메서드 사용
        feature.changeFacility(facility);
        feature.changeAsset(asset);

        // 저장
        Feature savedFeature = featureRepository.save(feature);
        log.debug("피처 저장 완료: id={}", savedFeature.getId());

        FileResponse assetFileResponse = assetService.getFileResponse(asset);
        FileResponse assetthumbnailFileResponse = assetService.getThumbnailFileResponse(asset);
        FileResponse facilityDrawingFileResponse = facilityService.getDrawingFileResponse(facility);
        FileResponse facilityThumbnailFileResponse = facilityService.getThumbnailFileResponse(facility);

        return FeatureResponse.from(
                savedFeature,
                assetFileResponse,
                assetthumbnailFileResponse,
                facilityDrawingFileResponse,
                facilityThumbnailFileResponse);
    }

    @Transactional(readOnly = true)
    public FeatureResponse getFeature(String id) {
        Feature feature = findFeatureById(id);
        return getFeatureResponse(feature);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> getFeatures() {
        List<Feature> features = featureRepository.findAll();
        return features.stream().map(this::getFeatureResponse).toList();
    }

    @Transactional
    public FeatureResponse updateFeature(String id, FeatureUpdateRequest request) {
        Feature feature = findFeatureById(id);
        feature.update(request);
        return getFeatureResponse(feature);
    }

    @Transactional
    public void deleteFeature(String id) {
        Feature feature = findFeatureById(id);

        // 로깅 추가
        log.info("피처 [{}] 삭제 전 연관관계 정리 시작", id);

        // 모든 연관관계 제거
        feature.clearAllRelations();

        log.info("피처 [{}]의 모든 연관관계 제거 완료, 삭제 진행", id);
        featureRepository.delete(feature);
    }

    @Transactional
    public void deleteFeatureWithRelations(String id) {
        deleteFeature(id);
    }

    @Transactional(readOnly = true)
    public Feature findFeatureById(String id) {
        return featureRepository.findById(id).orElseThrow(featureNotFound());
    }

    private static Supplier<CustomException> featureNotFound() {
        return () -> new CustomException("Feature not found", HttpStatus.NOT_FOUND, "해당 피처를 찾을 수 없습니다");
    }

    @Transactional
    public FeatureResponse assignAssetToFeature(String featureId, Long assetId) {
        log.debug("피처에 에셋 할당: featureId={}, assetId={}", featureId, assetId);

        Feature feature = findFeatureById(featureId);
        Asset asset =
                assetRepository
                        .findById(assetId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Asset not found", HttpStatus.NOT_FOUND, "해당 에셋을 찾을 수 없습니다: " + assetId));

        // 양방향 연관관계 설정 - 엔티티의 편의 메서드 사용
        feature.changeAsset(asset);
        log.debug("새 에셋과 피처 관계 설정: assetId={}, featureId={}", assetId, featureId);

        return getFeatureResponse(feature);
    }

    @Transactional
    public FeatureResponse removeAssetFromFeature(String featureId) {
        Feature feature = findFeatureById(featureId);

        if (feature.getAsset() == null) {
            throw new CustomException(
                    "No Asset Assigned to Feature",
                    HttpStatus.BAD_REQUEST,
                    String.format("피처 ID [%s]에 할당된 에셋이 없습니다", featureId));
        }

        Long assetId = feature.getAsset().getId();
        // 양방향 연관관계 제거 - 엔티티의 편의 메서드 사용
        feature.changeAsset(null);
        log.debug("피처에서 에셋 제거: featureId={}, assetId={}", featureId, assetId);

        return getFeatureResponse(feature);
    }

    @Transactional
    public FeatureResponse assignDeviceToFeature(String featureId, FeatureAssignDto assignDto) {
        log.debug("피처에 디바이스 할당: featureId={}, assignDto={}", featureId, assignDto);

        Feature feature = findFeatureById(featureId);

        // 디바이스 조회 - id로 조회
        Device device = findDeviceById(assignDto.id());

        if (device.getFeature() != null) {
            throw new CustomException(
                    "Device already assigned to another feature",
                    HttpStatus.BAD_REQUEST,
                    String.format("디바이스 ID [%s]는 이미 다른 피처에 할당되어 있습니다", assignDto.id()));
        }

        device.changeFeature(feature);

        log.debug("디바이스와 피처 관계 설정 완료: deviceId={}, featureId={}", device.getId(), featureId);

        // 업데이트된 피처 조회 및 반환
        feature = findFeatureById(featureId);
        return getFeatureResponse(feature);
    }

    private Device findDeviceById(String deviceId) {
        Device device = entityManager.find(Device.class, deviceId);

        if (device == null) {
            throw new CustomException(
                    "Device not found", HttpStatus.NOT_FOUND, "해당 디바이스를 찾을 수 없습니다. ID: " + deviceId);
        }

        return device;
    }

    @Transactional
    public FeatureResponse removeDeviceFromFeature(String featureId, FeatureAssignDto assignDto) {
        Feature feature = findFeatureById(featureId);

        if (feature.getDevice() == null) {
            throw new CustomException(
                    "No Device Assigned to Feature",
                    HttpStatus.BAD_REQUEST,
                    String.format("피처 ID [%s]에 할당된 디바이스가 없습니다", featureId));
        }

        // 특정 디바이스 ID로 검증
        boolean isMatchingDevice = assignDto.id().equals(feature.getDevice().getId());

        // 일치하는 디바이스가 아닌 경우 예외 발생
        if (!isMatchingDevice) {
            throw new CustomException(
                    "Device mismatch", HttpStatus.BAD_REQUEST, "요청한 디바이스가 현재 피처에 할당된 디바이스와 일치하지 않습니다");
        }

        String deviceId = feature.getDevice().getId();
        feature.changeDevice(null);
        log.debug("피처에서 디바이스 제거: featureId={}, deviceId={}", featureId, deviceId);

        return getFeatureResponse(feature);
    }

    // 기존 메서드 유지 (하위 호환성)
    @Transactional
    public FeatureResponse removeDeviceFromFeature(String featureId) {
        Feature feature = findFeatureById(featureId);

        if (feature.getDevice() == null) {
            throw new CustomException(
                    "No Device Assigned to Feature",
                    HttpStatus.BAD_REQUEST,
                    String.format("피처 ID [%s]에 할당된 디바이스가 없습니다", featureId));
        }

        String deviceId = feature.getDevice().getId();
        feature.changeDevice(null);
        log.debug("피처에서 디바이스 제거: featureId={}, deviceId={}", featureId, deviceId);

        return getFeatureResponse(feature);
    }

    private FeatureResponse getFeatureResponse(Feature feature) {
        FileResponse assetFileResponse = assetService.getFileResponse(feature.getAsset());
        FileResponse assetthumbnailFileResponse =
                assetService.getThumbnailFileResponse(feature.getAsset());
        FileResponse facilityDrawingFileResponse =
                facilityService.getDrawingFileResponse(feature.getFacility());
        FileResponse facilityThumbnailFileResponse =
                facilityService.getThumbnailFileResponse(feature.getFacility());
        return FeatureResponse.from(
                feature,
                assetFileResponse,
                assetthumbnailFileResponse,
                facilityDrawingFileResponse,
                facilityThumbnailFileResponse);
    }

    @Transactional
    public Feature saveFeature(Feature feature) {
        return featureRepository.save(feature);
    }
}
