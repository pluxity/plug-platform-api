package com.pluxity.feature.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.asset.service.AssetService;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityRepository;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
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

        // Asset과의 양방향 관계 제거 - 엔티티의 편의 메서드 사용
        if (feature.getAsset() != null) {
            feature.changeAsset(null);
        }

        featureRepository.delete(feature);
    }

    private Feature findFeatureById(String id) {
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
    public FeatureResponse assignDeviceToFeature(String featureId, Long deviceId) {
        log.debug("피처에 디바이스 할당: featureId={}, deviceId={}", featureId, deviceId);

        Feature feature = findFeatureById(featureId);

        // 이미 할당된 디바이스가 있는지 확인
        if (feature.getDevice() != null) {
            if (feature.getDevice().getId().equals(deviceId)) {
                log.debug("이미 해당 디바이스가 할당되어 있습니다: featureId={}, deviceId={}", featureId, deviceId);
                return getFeatureResponse(feature);
            }
            throw new CustomException(
                    "Feature already assigned to another device",
                    HttpStatus.BAD_REQUEST,
                    "이미 다른 디바이스에 할당된 피처입니다: " + featureId);
        }

        // 디바이스 조회 로직 - 디바이스 서비스를 통해 조회해야 하지만,
        // 순환 참조 문제를 피하기 위해 여기서는 예외만 던집니다.
        // 실제 구현은 NfluxService의 assignFeatureToNflux 메서드를 통해 수행
        throw new CustomException(
                "Operation not supported", HttpStatus.BAD_REQUEST, "디바이스에서 피처를 할당하는 API를 사용해주세요.");
    }

    @Transactional
    public FeatureResponse removeDeviceFromFeature(String featureId) {
        Feature feature = findFeatureById(featureId);

        if (feature.getDevice() == null) {
            throw new CustomException(
                    "No Device Assigned to Feature",
                    HttpStatus.BAD_REQUEST,
                    String.format("피처 ID [%s]에 할당된 디바이스가 없습니다", featureId));
        }

        Long deviceId = feature.getDevice().getId();
        // 양방향 연관관계 제거 - 엔티티의 편의 메서드 사용
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
}
