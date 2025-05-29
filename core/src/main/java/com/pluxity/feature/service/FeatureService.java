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
        Feature feature = Feature.create(request, request.id());

        Facility facility = facilityService.findById(request.facilityId());
        feature.changeFacility(facility);

        Asset asset = assetService.findById(request.assetId());
        feature.changeAsset(asset);

        FileResponse assetFileResponse = assetService.getFileResponse(asset);
        FileResponse assetthumbnailFileResponse = assetService.getThumbnailFileResponse(asset);
        FileResponse facilityDrawingFileResponse = facilityService.getDrawingFileResponse(facility);
        FileResponse facilityThumbnailFileResponse = facilityService.getThumbnailFileResponse(facility);

        Feature savedFeature = featureRepository.save(feature);
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
        Asset asset =
                assetRepository
                        .findById(assetId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Asset not found", HttpStatus.NOT_FOUND, "해당 에셋을 찾을 수 없습니다: " + assetId));

        feature.changeAsset(asset); // 엔티티의 편의 메서드 사용
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

        feature.changeAsset(null); // 엔티티의 편의 메서드 사용 (Asset과의 연결 해제)
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
