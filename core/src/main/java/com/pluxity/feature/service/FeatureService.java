package com.pluxity.feature.service;

import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final FeatureRepository featureRepository;

    @Transactional
    public FeatureResponse createFeature(FeatureCreateRequest request) {
        Feature feature = Feature.create(request);
        Feature savedFeature = featureRepository.save(feature);
        return FeatureResponse.from(savedFeature);
    }

    @Transactional(readOnly = true)
    public FeatureResponse getFeature(Long id) {
        Feature feature = findFeatureById(id);
        return FeatureResponse.from(feature);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> getFeatures() {
        List<Feature> features = featureRepository.findAll();
        return features.stream()
                .map(FeatureResponse::from)
                .toList();
    }

    @Transactional
    public FeatureResponse updateFeature(Long id, FeatureUpdateRequest request) {
        Feature feature = findFeatureById(id);
        feature.update(request);
        return FeatureResponse.from(feature);
    }

    @Transactional
    public void deleteFeature(Long id) {
        Feature feature = findFeatureById(id);
        featureRepository.delete(feature);
    }

    private Feature findFeatureById(Long id) {
        return featureRepository.findById(id)
                .orElseThrow(featureNotFound());
    }

    private static Supplier<CustomException> featureNotFound() {
        return () -> new CustomException("Feature not found", HttpStatus.NOT_FOUND, "해당 피처를 찾을 수 없습니다");
    }
}
