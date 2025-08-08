package com.pluxity.feature.service;

import static com.pluxity.global.constant.ErrorCode.CCTV_ALREADY_HAS_FEATURE;
import static com.pluxity.global.constant.ErrorCode.CCTV_MISMATCH;

import com.pluxity.cctv.Cctv;
import com.pluxity.cctv.CctvService;
import com.pluxity.feature.dto.CctvAssignDto;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.FeatureType;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFacade {

    private final FeatureService featureService;
    private final CctvService cctvService;

    @Transactional
    public void assignCctvToFeature(String featureId, CctvAssignDto assignDto) {
        log.debug("피처에 CCTV 할당: featureId={}, assignDto={}", featureId, assignDto);

        Feature feature = featureService.findFeatureById(featureId);
        Cctv cctv = cctvService.findById(assignDto.id());

        if (cctv.getFeature() != null) {
            throw new CustomException(CCTV_ALREADY_HAS_FEATURE, cctv.getFeature().getId());
        }

        cctv.changeFeature(feature);
        feature.updateFeatureType(FeatureType.CCTV);

        log.debug("CCTV와 피처 관계 설정 완료: cctvId={}, featureId={}", cctv.getId(), featureId);
    }

    @Transactional
    public void removeCctvFromFeature(String featureId, CctvAssignDto assignDto) {
        Feature feature = featureService.findFeatureById(featureId);
        Cctv cctv = cctvService.findById(assignDto.id());

        // 특정 CCTV ID로 검증
        String cctvId = cctv.getId();
        boolean isMatchingCctv = assignDto.id().equals(cctvId);

        // 일치하는 CCTV가 아닌 경우 예외 발생
        if (!isMatchingCctv) {
            throw new CustomException(CCTV_MISMATCH);
        }

        cctv.changeFeature(null);
        feature.updateFeatureType(FeatureType.NONE);
        log.debug("피처에서 CCTV 제거: featureId={}, cctvId={}", featureId, cctvId);
    }
}
