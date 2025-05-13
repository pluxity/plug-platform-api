package com.pluxity.asset.service;

import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.entity.FileEntity;
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
public class AssetService {

    private final AssetRepository assetRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public AssetResponse getAsset(Long id) {
        Asset asset = findAssetById(id);
        FileResponse assetFileResponse = getFileResponse(asset);
        return AssetResponse.from(asset, assetFileResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssets() {
        List<Asset> assets = assetRepository.findAll();
        return assets.stream().map(asset -> AssetResponse.from(asset, getFileResponse(asset))).toList();
    }

    @Transactional
    public Long createAsset(AssetCreateRequest request) {
        Asset asset = Asset.create(request);
        Asset savedAsset = assetRepository.save(asset);

        if (request.fileId() != null) {
            String filePath = savedAsset.getAssetFilePath();
            FileEntity fileEntity = fileService.finalizeUpload(request.fileId(), filePath);
            savedAsset.updateFileEntity(fileEntity);
        }

        return savedAsset.getId();
    }

    @Transactional
    public void updateAsset(Long id, AssetUpdateRequest request) {
        Asset asset = findAssetById(id);

        asset.update(request);

        if (request.fileId() != null) {
            FileEntity fileEntity =
                    fileService.finalizeUpload(request.fileId(), asset.getAssetFilePath());
            asset.updateFileEntity(fileEntity);
        }
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = findAssetById(id);
        assetRepository.delete(asset);
    }

    private Asset findAssetById(Long id) {
        return assetRepository.findById(id).orElseThrow(notFoundAsset());
    }

    private FileResponse getFileResponse(Asset asset) {
        if (!asset.hasFile()) {
            return null;
        }
        return fileService.getFileResponse(asset.getFileId());
    }

    private static Supplier<CustomException> notFoundAsset() {
        return () -> new CustomException("Asset not found", HttpStatus.NOT_FOUND, "해당 자원을 찾을 수 없습니다");
    }
}
