package com.pluxity.feature.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.asset.service.AssetValidator;
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
    private final FeatureAssignmentRegistry registry;

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
    public void assignSomethingToFeature(
            String featureId, FeatureAssignDto assignDto, boolean force) {
        Feature feature = findFeatureById(featureId);
        FeatureAssignment assignmentTarget = getAssignmentTarget(assignDto.type());
        if (!force && assignmentTarget.isAssigned(assignDto.id())) {
            throw new CustomException(
                    ALREADY_ASSIGNED_TARGET, assignDto.id(), assignDto.type().getDescription());
        }
        if (!force && checkFeatureAlreadyOther(feature)) {
            throw new CustomException(ALREADY_FEATURE_ASSIGNED, featureId);
        }
        revokeByFeatureAll(feature);
        assignmentTarget.assignFeature(assignDto.id(), feature);
    }

    @Transactional
    public void removeSomethingFromFeature(String featureId, FeatureAssignDto assignDto) {
        findFeatureById(featureId);
        FeatureAssignment assignmentTarget = getAssignmentTarget(assignDto.type());
        assignmentTarget.validateRevoke(assignDto.id(), featureId);
        assignmentTarget.clearFeatureFromTarget(assignDto.id());
    }

    private boolean checkFeatureAlreadyOther(Feature feature) {
        return registry.all().stream().anyMatch(assignment -> assignment.existsByFeature(feature));
    }

    private void revokeByFeatureAll(Feature feature) {
        registry.all().forEach(v -> v.revokeByFeature(feature));
    }

    private FeatureAssignment getAssignmentTarget(FeatureAssignType type) {
        return registry.get(type);
    }
}
