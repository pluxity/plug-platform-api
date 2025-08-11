package com.pluxity.feature.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.asset.service.AssetValidator;
import com.pluxity.device.entity.Device;
import com.pluxity.device.repository.DeviceRepository;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.feature.dto.FeatureAssignDto;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final FeatureRepository featureRepository;
    private final FacilityService facilityService;
    private final AssetValidator assetValidator;
    private final DeviceRepository deviceRepository;

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
            throw new CustomException(DUPLICATE_FEATURE_ID, featureId);
        }

        // 먼저 관련 엔티티 조회
        Facility facility = facilityService.findById(request.facilityId());
        assetValidator.validateAssetId(request.assetId());

        // 저장
        Feature savedFeature = featureRepository.save(Feature.create(request, featureId, facility));
        log.debug("피처 저장 완료: id={}", savedFeature.getId());

        return getFeatureResponse(savedFeature);
    }

    @Transactional(readOnly = true)
    public FeatureResponse getFeature(String id) {
        Feature feature = findFeatureById(id);
        return getFeatureResponse(feature);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> getFeatures(Long facilityId) {
        Facility facility = facilityService.findById(facilityId);
        List<Feature> features = featureRepository.findByFacilityOrderByCreatedAtDesc(facility);
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
        featureRepository.delete(feature);
    }

    @Transactional(readOnly = true)
    public Feature findFeatureById(String id) {
        return featureRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FEATURE, id));
    }

    @Transactional
    public void assignDeviceToFeature(String featureId, FeatureAssignDto assignDto) {
        log.debug("피처에 디바이스 할당: featureId={}, assignDto={}", featureId, assignDto);

        Feature feature = findFeatureById(featureId);

        // 디바이스 조회 - id로 조회
        Device device = findDeviceById(assignDto.id());

        if (device.getFeature() != null) {
            throw new CustomException(DEVICE_ALREADY_HAS_FEATURE, device.getFeature().getId());
        }

        deviceRepository
                .findByFeature(feature)
                .ifPresent(
                        v -> {
                            throw new CustomException(DUPLICATE_ASSIGN_OTHER_DEVICE, feature.getId(), v.getId());
                        });

        device.changeFeature(feature);

        log.debug("디바이스와 피처 관계 설정 완료: deviceId={}, featureId={}", device.getId(), featureId);
    }

    private Device findDeviceById(String deviceId) {
        return deviceRepository
                .findById(deviceId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_DEVICE, deviceId));
    }

    @Transactional
    public void removeDeviceFromFeature(String featureId, FeatureAssignDto assignDto) {
        Feature feature = findFeatureById(featureId);
        Device device = findDeviceById(assignDto.id());

        // 특정 디바이스 ID로 검증
        String deviceId = device.getId();
        boolean isMatchingDevice = assignDto.id().equals(deviceId);

        // 일치하는 디바이스가 아닌 경우 예외 발생
        if (!isMatchingDevice) {
            throw new CustomException(DEVICE_MISMATCH);
        }

        device.changeFeature(null);
        log.debug("피처에서 디바이스 제거: featureId={}, deviceId={}", featureId, deviceId);
    }

    private FeatureResponse getFeatureResponse(Feature feature) {
        return FeatureResponse.from(feature);
    }

    @Transactional
    public Feature saveFeature(Feature feature) {
        return featureRepository.save(feature);
    }

    @Transactional(readOnly = true)
    public List<String> findFeatureIdsByAssetId(Long assetId) {
        return featureRepository.findByAssetId(assetId).stream().map(Feature::getId).toList();
    }
}
