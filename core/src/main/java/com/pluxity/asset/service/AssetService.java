package com.pluxity.asset.service;

import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.entity.AssetCategory;
import com.pluxity.asset.repository.AssetCategoryRepository;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final FileService fileService;
    private final AssetCategoryService assetCategoryService;
    private FeatureService featureService;

    @Autowired
    public void setFeatureService(@Lazy FeatureService featureService) {
        this.featureService = featureService;
    }

    public AssetService(
            AssetRepository assetRepository,
            AssetCategoryRepository assetCategoryRepository,
            FileService fileService,
            AssetCategoryService assetCategoryService) {
        this.assetRepository = assetRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.fileService = fileService;
        this.assetCategoryService = assetCategoryService;
    }

    @Transactional(readOnly = true)
    public AssetResponse getAsset(Long id) {
        Asset asset = findById(id);
        FileResponse assetFileResponse = getFileResponse(asset);
        FileResponse thumbnailFileResponse = getThumbnailFileResponse(asset);
        return AssetResponse.from(asset, assetFileResponse, thumbnailFileResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssets() {
        List<Asset> assets = assetRepository.findAll();
        return assets.stream()
                .map(
                        asset ->
                                AssetResponse.from(asset, getFileResponse(asset), getThumbnailFileResponse(asset)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssetsByCategory(Long categoryId) {
        AssetCategory category = assetCategoryService.findById(categoryId);
        List<Asset> assets = assetRepository.findByCategory(category);
        return assets.stream()
                .map(
                        asset ->
                                AssetResponse.from(asset, getFileResponse(asset), getThumbnailFileResponse(asset)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetByCode(String code) {
        Asset asset =
                assetRepository
                        .findByCode(code)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Asset Not Found By Code",
                                                HttpStatus.NOT_FOUND,
                                                String.format("코드가 %s인 에셋을 찾을 수 없습니다", code)));
        FileResponse assetFileResponse = getFileResponse(asset);
        FileResponse thumbnailFileResponse = getThumbnailFileResponse(asset);
        return AssetResponse.from(asset, assetFileResponse, thumbnailFileResponse);
    }

    @Transactional
    public Long createAsset(@Valid AssetCreateRequest request) {
        createValidation(request);

        Asset asset = Asset.create(request);

        if (request.categoryId() != null) {
            AssetCategory category =
                    assetCategoryRepository
                            .findById(request.categoryId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(
                                                    "Category Not Found",
                                                    HttpStatus.NOT_FOUND,
                                                    String.format("ID가 %d인 카테고리를 찾을 수 없습니다", request.categoryId())));
            asset.updateCategory(category);
        }

        Asset savedAsset = assetRepository.save(asset);

        if (request.fileId() != null) {
            String filePath = savedAsset.getAssetFilePath();
            FileEntity fileEntity = fileService.finalizeUpload(request.fileId(), filePath);
            savedAsset.updateFileEntity(fileEntity);
        }

        if (request.thumbnailFileId() != null) {
            String thumbnailPath = savedAsset.getThumbnailFilePath();
            FileEntity thumbnailEntity =
                    fileService.finalizeUpload(request.thumbnailFileId(), thumbnailPath);
            savedAsset.updateThumbnailFileEntity(thumbnailEntity);
        }

        return savedAsset.getId();
    }

    private void createValidation(AssetCreateRequest request) {
        assetRepository
                .findByName(request.name())
                .ifPresent(
                        asset -> {
                            throw new CustomException(
                                    "Asset Already Exists",
                                    HttpStatus.BAD_REQUEST,
                                    String.format("이름이 %s인 에셋이 이미 존재합니다", request.name()));
                        });
        assetRepository
                .findByCode(request.code())
                .ifPresent(
                        asset -> {
                            throw new CustomException(
                                    "Code Already Exists",
                                    HttpStatus.BAD_REQUEST,
                                    String.format("코드가 %s인 에셋이 이미 존재합니다", request.code()));
                        });
    }

    @Transactional
    public void updateAsset(Long id, @Valid AssetUpdateRequest request) {

        updateValidation(id, request);
        Asset asset = findById(id);

        asset.update(request);

        if (request.categoryId() != null) {
            AssetCategory category =
                    assetCategoryRepository
                            .findById(request.categoryId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(
                                                    "Category Not Found",
                                                    HttpStatus.NOT_FOUND,
                                                    String.format("ID가 %d인 카테고리를 찾을 수 없습니다", request.categoryId())));
            asset.updateCategory(category);
        }

        if (request.fileId() != null) {
            FileEntity fileEntity =
                    fileService.finalizeUpload(request.fileId(), asset.getAssetFilePath());
            asset.updateFileEntity(fileEntity);
        }

        if (request.thumbnailFileId() != null) {
            FileEntity thumbnailEntity =
                    fileService.finalizeUpload(request.thumbnailFileId(), asset.getThumbnailFilePath());
            asset.updateThumbnailFileEntity(thumbnailEntity);
        }
    }

    private void updateValidation(Long id, AssetUpdateRequest request) {
        assetRepository
                .findByNameAndIdNot(request.name(), id)
                .ifPresent(
                        asset -> {
                            throw new CustomException(
                                    "Asset Already Exists",
                                    HttpStatus.BAD_REQUEST,
                                    String.format("이름이 %s인 에셋이 이미 존재합니다", request.name()));
                        });
        assetRepository
                .findByCodeAndIdNot(request.code(), id)
                .ifPresent(
                        asset -> {
                            throw new CustomException(
                                    "Code Already Exists",
                                    HttpStatus.BAD_REQUEST,
                                    String.format("코드가 %s인 에셋이 이미 존재합니다", request.code()));
                        });
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = findById(id);

        List<String> featureIds = asset.getAllFeatures().stream().map(Feature::getId).toList();
        int featureCount = featureIds.size();

        log.info("에셋 [{}] 삭제 전 연관관계 정리 시작 (연결된 피처: {}개)", id, featureCount);

        // 모든 연관관계 제거
        asset.clearAllRelations();
        log.info("에셋 [{}]의 모든 연관관계 제거 완료", id);

        // Feature 삭제
        if (!featureIds.isEmpty()) {
            log.info("에셋 [{}]에 연결되었던 피처 [{}]개 삭제 시작", id, featureCount);
            for (String featureId : featureIds) {
                featureService.deleteFeature(featureId);
            }
            log.info("에셋 [{}]에 연결되었던 피처 모두 삭제 완료", id);
        }

        log.info("에셋 [{}] 삭제 진행", id);
        assetRepository.delete(asset);
    }

    @Transactional
    public void assignCategory(Long assetId, Long categoryId) {
        Asset asset = findById(assetId);
        AssetCategory category = assetCategoryService.findById(categoryId);

        asset.updateCategory(category);
        log.info("에셋 [{}]에 카테고리 [{}]가 할당되었습니다.", assetId, categoryId);
    }

    @Transactional
    public void removeCategory(Long assetId) {
        Asset asset = findById(assetId);

        if (asset.getCategory() == null) {
            throw new CustomException(
                    "No Category Assigned",
                    HttpStatus.BAD_REQUEST,
                    String.format("에셋 [%d]에 할당된 카테고리가 없습니다", assetId));
        }

        asset.updateCategory(null);
        log.info("에셋 [{}]에서 카테고리가 제거되었습니다.", assetId);
    }

    @Transactional
    public Asset findById(Long id) {
        return assetRepository.findById(id).orElseThrow(notFoundAsset());
    }

    public FileResponse getFileResponse(Asset asset) {
        if (asset == null) {
            return null;
        }

        if (!asset.hasFile()) {
            return null;
        }
        return fileService.getFileResponse(asset.getFileId());
    }

    public FileResponse getThumbnailFileResponse(Asset asset) {
        if (asset == null) {
            return null;
        }
        if (!asset.hasThumbnail()) {
            return null;
        }
        return fileService.getFileResponse(asset.getThumbnailFileId());
    }

    private static Supplier<CustomException> notFoundAsset() {
        return () -> new CustomException("Asset not found", HttpStatus.NOT_FOUND, "해당 자원을 찾을 수 없습니다");
    }
}
