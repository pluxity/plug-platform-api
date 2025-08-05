package com.pluxity.asset.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.asset.dto.AssetCategoryAllResponse;
import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryUpdateRequest;
import com.pluxity.asset.entity.AssetCategory;
import com.pluxity.asset.repository.AssetCategoryRepository;
import com.pluxity.category.service.CategoryService;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.global.utils.SortUtils;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetCategoryService extends CategoryService<AssetCategory> {

    private final AssetCategoryRepository assetCategoryRepository;
    private final FileService fileService;

    @Override
    protected JpaRepository<AssetCategory, Long> getRepository() {
        return assetCategoryRepository;
    }

    @Transactional(readOnly = true)
    public AssetCategoryResponse getAssetCategory(Long id) {
        AssetCategoryAllResponse allCategories = getAllCategories();
        return allCategories.list().stream()
                .filter(v -> v.id().equals(id))
                .findFirst()
                .orElseThrow(notFoundAssetCategory(id));
    }

    @Transactional(readOnly = true)
    public AssetCategoryAllResponse getAllCategories() {
        List<AssetCategory> allCategories =
                assetCategoryRepository.findAll(SortUtils.getOrderByCreatedAtDesc());
        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(allCategories, v -> Stream.of(v.getIconFileId()), fileService);
        List<AssetCategoryResponse> list =
                allCategories.stream()
                        .map(v -> AssetCategoryResponse.from(v, fileMap.get(v.getIconFileId())))
                        .toList();

        return AssetCategoryAllResponse.of(
                AssetCategory.builder().build().getMaxDepth(),
                MappingUtils.makeCategoryTree(
                        list,
                        AssetCategoryResponse::id,
                        AssetCategoryResponse::parentId,
                        AssetCategoryResponse::children));
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryResponse> getChildCategories(Long parentId) {
        List<AssetCategory> childCategories = assetCategoryRepository.findByParentId(parentId);
        return childCategories.stream().map(this::createAssetCategoryResponseWithoutChildren).toList();
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
        if (!category.getCode().equals(request.code())) {
            validateCodeUniqueness(request.code());
        }
        super.update(id, request.name(), request.parentId());
        category.updateCode(request.code());
        category.updateIconFileId(request.thumbnailFileId());
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
