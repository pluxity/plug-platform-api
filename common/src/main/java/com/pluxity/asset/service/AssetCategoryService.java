package com.pluxity.asset.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.asset.dto.AssetCategoryAllResponse;
import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryUpdateRequest;
import com.pluxity.asset.entity.AssetCategory;
import com.pluxity.asset.repository.AssetCategoryRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetCategoryService {

    private final AssetCategoryRepository assetCategoryRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public AssetCategoryResponse getAssetCategory(Long id) {
        AssetCategory category = findById(id);
        return createAssetCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public AssetCategoryAllResponse getAllCategories() {
        List<AssetCategory> rootCategories = assetCategoryRepository.findAllRootCategories();
        List<AssetCategoryResponse> list =
                  rootCategories.stream().map(this::createAssetCategoryResponse).toList();
        return AssetCategoryAllResponse.of(AssetCategory.builder().build().getMaxDepth(), list);
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryResponse> getChildCategories(Long parentId) {
        List<AssetCategory> childCategories = assetCategoryRepository.findByParentId(parentId);
        return childCategories.stream().map(this::createAssetCategoryResponseWithoutChildren).toList();
    }

    private AssetCategoryResponse createAssetCategoryResponse(AssetCategory category) {
        return AssetCategoryResponse.from(
                category,
                category.getIconFileId() != null
                        ? fileService.getFileResponse(category.getIconFileId())
                        : null);
    }

    private AssetCategoryResponse createAssetCategoryResponseWithoutChildren(AssetCategory category) {
        return AssetCategoryResponse.fromWithoutChildren(
                category,
                category.getIconFileId() != null
                        ? fileService.getFileResponse(category.getIconFileId())
                        : null);
    }

    @Transactional
    public Long createAssetCategory(AssetCategoryCreateRequest request) {
        validateCodeUniqueness(request.code());

        AssetCategory parent = null;
        if (request.parentId() != null) {
            parent = findById(request.parentId());
        }

        AssetCategory category =
                AssetCategory.builder().name(request.name()).code(request.code()).parent(parent).build();

        if (request.thumbnailFileId() != null) {
            category.updateIconFileId(request.thumbnailFileId());
        }

        AssetCategory savedCategory = assetCategoryRepository.save(category);
        return savedCategory.getId();
    }

    @Transactional
    public void updateAssetCategory(Long id, AssetCategoryUpdateRequest request) {
        AssetCategory category = findById(id);

        if (request.code() != null && !request.code().equals(category.getCode())) {
            validateCodeUniqueness(request.code());
            category.updateCode(request.code());
        }

        if (request.name() != null) {
            category.updateName(request.name());
        }

        if (request.thumbnailFileId() != null) {
            category.updateIconFileId(request.thumbnailFileId());
        }

        if (request.parentId() != null) {
            AssetCategory parent = findById(request.parentId());
            if (parent.getId().equals(category.getId())) {
                throw new CustomException(INVALID_PARENT_CATEGORY);
            }
            category.assignToParent(parent);
        }
    }

    @Transactional
    public void deleteAssetCategory(Long id) {
        AssetCategory category = findById(id);

        if (!category.getAssets().isEmpty()) {
            throw new CustomException(ASSET_CATEGORY_HAS_ASSET);
        }

        if (!category.getChildren().isEmpty()) {
            throw new CustomException(ASSET_CATEGORY_HAS_CHILDREN);
        }

        assetCategoryRepository.delete(category);
    }

    @Transactional
    public AssetCategory findById(Long id) {
        return assetCategoryRepository.findById(id).orElseThrow(notFoundAssetCategory(id));
    }

    private Supplier<CustomException> notFoundAssetCategory(Long id) {
        return () -> new CustomException(NOT_FOUND_ASSET_CATEGORY, id);
    }

    private void validateCodeUniqueness(String code) {
        if (assetCategoryRepository.existsByCode(code)) {
            throw new CustomException(DUPLICATE_ASSET_CATEGORY_CODE, code);
        }
    }
}
