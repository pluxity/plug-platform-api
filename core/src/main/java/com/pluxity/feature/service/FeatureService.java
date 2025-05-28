package com.pluxity.feature.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import java.util.List;
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

    @Transactional
    public FeatureResponse createFeature(FeatureCreateRequest request) {
        Feature feature = Feature.create(request, request.id());

        if (request.assetId() != null) {
            Asset asset =
                    assetRepository
                            .findById(request.assetId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(
                                                    "Asset not found", HttpStatus.NOT_FOUND, "해당 에셋을 찾을 수 없습니다."));
            feature.changeAsset(asset);
        }

        Feature savedFeature = featureRepository.save(feature);
        return FeatureResponse.from(savedFeature);
    }

    @Transactional(readOnly = true)
    public FeatureResponse getFeature(String id) {
        Feature feature = findFeatureById(id);
        return FeatureResponse.from(feature);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> getFeatures() {
        List<Feature> features = featureRepository.findAll();
        return features.stream().map(FeatureResponse::from).toList();
    }

    @Transactional
    public FeatureResponse updateFeature(String id, FeatureUpdateRequest request) {
        Feature feature = findFeatureById(id);
        feature.update(request);

        if (request.assetId() != null) {
            Asset asset =
                    assetRepository
                            .findById(request.assetId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(
                                                    "Asset not found", HttpStatus.NOT_FOUND, "해당 에셋을 찾을 수 없습니다."));
            feature.changeAsset(asset);
        }

        return FeatureResponse.from(feature);
    }

    @Transactional
    public void deleteFeature(String id) {
        Feature feature = findFeatureById(id);
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
        Feature feature = findFeatureById(featureId);
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new CustomException("Asset not found", HttpStatus.NOT_FOUND, "해당 에셋을 찾을 수 없습니다: " + assetId));
        
        feature.changeAsset(asset); // 엔티티의 편의 메서드 사용
        // featureRepository.save(feature); // 변경 감지로 처리될 수 있음
        return FeatureResponse.from(feature);
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

        feature.changeAsset(null); // 엔티티의 편의 메서드 사용 (Asset과의 연결 해제)
        // featureRepository.save(feature);
        return FeatureResponse.from(feature);
    }
}
