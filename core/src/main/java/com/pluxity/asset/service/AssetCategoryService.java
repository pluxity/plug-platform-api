package com.pluxity.asset.service;

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
import org.springframework.http.HttpStatus;
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
    public List<AssetCategoryResponse> getAssetCategories() {
        List<AssetCategory> categories = assetCategoryRepository.findAll();
        return categories.stream().map(this::createAssetCategoryResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryResponse> getRootCategories() {
        List<AssetCategory> rootCategories = assetCategoryRepository.findAllRootCategories();
        return rootCategories.stream().map(this::createAssetCategoryResponse).toList();
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

        if (request.iconFileId() != null) {
            category.updateIconFileId(request.iconFileId());
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

        if (request.iconFileId() != null) {
            category.updateIconFileId(request.iconFileId());
        }

        if (request.parentId() != null) {
            AssetCategory parent = findById(request.parentId());
            if (parent.getId().equals(category.getId())) {
                throw new CustomException(
                        "Invalid Parent", HttpStatus.BAD_REQUEST, "카테고리는 자기 자신을 부모로 가질 수 없습니다.");
            }
            category.assignToParent(parent);
        }
    }

    @Transactional
    public void deleteAssetCategory(Long id) {
        AssetCategory category = findById(id);

        if (!category.getAssets().isEmpty()) {
            throw new CustomException(
                    "Cannot Delete Category", HttpStatus.BAD_REQUEST, "카테고리에 속한 에셋이 있어 삭제할 수 없습니다.");
        }

        if (!category.getChildren().isEmpty()) {
            throw new CustomException(
                    "Cannot Delete Category", HttpStatus.BAD_REQUEST, "하위 카테고리가 있어 삭제할 수 없습니다.");
        }

        assetCategoryRepository.delete(category);
    }

    @Transactional
    public AssetCategory findById(Long id) {
        return assetCategoryRepository.findById(id).orElseThrow(notFoundAssetCategory(id));
    }

    private Supplier<CustomException> notFoundAssetCategory(Long id) {
        return () ->
                new CustomException(
                        "Asset Category Not Found",
                        HttpStatus.NOT_FOUND,
                        String.format("ID가 %d인 에셋 카테고리를 찾을 수 없습니다", id));
    }

    private void validateCodeUniqueness(String code) {
        if (assetCategoryRepository.existsByCode(code)) {
            throw new CustomException(
                    "Duplicate Code", HttpStatus.BAD_REQUEST, String.format("이미 존재하는 코드입니다: %s", code));
        }
    }
}
