package com.pluxity.feature.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.asset.service.AssetValidator;
import com.pluxity.cctv.entity.Cctv;
import com.pluxity.cctv.repository.CctvRepository;
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
    private final CctvRepository cctvRepository;

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
    public void assignDeviceToFeature(String featureId, FeatureAssignDto assignDto, boolean force) {
        log.debug("피처에 디바이스 할당: featureId={}, assignDto={}", featureId, assignDto);

        Feature feature = findFeatureById(featureId);

        // 디바이스 조회 - id로 조회
        Device device = findDeviceById(assignDto.id());
        if (!force && device.getFeature() != null) {
            throw new CustomException(DUPLICATE_DEVICE_OTHER_FEATURE, assignDto.id());
        }
        boolean isAssignCctv = cctvRepository.existsByFeature(feature);
        if (!force && isAssignCctv) {
            throw new CustomException(DUPLICATE_FEATURE_OTHER_CCTV, featureId);
        }

        deviceRepository.updateFeatureByFeature(feature);
        cctvRepository.updateFeatureByFeature(feature);
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

        if (device.getFeature() == null) {
            throw new CustomException(DEVICE_NOT_ASSIGNED, device.getId());
        }

        if (!device.getFeature().getId().equals(feature.getId())) {
            throw new CustomException(DEVICE_MISMATCH, "해당 피처에 할당된 디바이스가 아닙니다.");
        }

        device.changeFeature(null);
        log.debug("피처에서 디바이스 제거: featureId={}, deviceId={}", featureId, device.getId());
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

    @Transactional
    public void assignCctvToFeature(String featureId, FeatureAssignDto assignDto, boolean force) {
        log.debug("피처에 Cctv 할당: featureId={}, assignDto={}", featureId, assignDto);

        Feature feature = findFeatureById(featureId);

        // Cctv 조회 - id로 조회
        Cctv cctv =
                cctvRepository
                        .findById(assignDto.id())
                        .orElseThrow(() -> new CustomException(NOT_FOUND_CCTV, assignDto.id()));
        if (!force && cctv.getFeature() != null) {
            throw new CustomException(DUPLICATE_CCTV_OTHER_FEATURE, assignDto.id());
        }
        boolean isAssignDevice = deviceRepository.existsByFeature(feature);
        if (!force && isAssignDevice) {
            throw new CustomException(DUPLICATE_FEATURE_OTHER_DEVICE, featureId);
        }

        deviceRepository.updateFeatureByFeature(feature);
        cctvRepository.updateFeatureByFeature(feature);
        cctv.changeFeature(feature);

        log.debug("Cctv와 피처 관계 설정 완료: cctvId={}, featureId={}", cctv.getId(), featureId);
    }

    @Transactional
    public void removeCctvFromFeature(String featureId, FeatureAssignDto assignDto) {
        Feature feature = findFeatureById(featureId);
        Cctv cctv =
                cctvRepository
                        .findById(assignDto.id())
                        .orElseThrow(() -> new CustomException(NOT_FOUND_CCTV, assignDto.id()));

        if (cctv.getFeature() == null) {
            throw new CustomException(CCTV_NOT_ASSIGNED, cctv.getId());
        }

        if (!cctv.getFeature().getId().equals(feature.getId())) {
            throw new CustomException(CCTV_MISMATCH);
        }

        cctv.changeFeature(null);
        log.debug("피처에서 Cctv 제거: featureId={}, cctvId={}", featureId, cctv.getId());
    }
}
