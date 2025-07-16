package com.pluxity.asset.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.asset.dto.AssetCategoryAllResponse;
import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryUpdateRequest;
import com.pluxity.asset.entity.AssetCategory;
import com.pluxity.asset.repository.AssetCategoryRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
        AssetCategoryAllResponse allCategories = getAllCategories();
        return allCategories.list().stream()
                .filter(v -> v.id().equals(id))
                .findFirst()
                .orElseThrow(notFoundAssetCategory(id));
    }

    @Transactional(readOnly = true)
    public AssetCategoryAllResponse getAllCategories() {
        List<AssetCategory> allCategories = assetCategoryRepository.findAll();
        List<Long> fileIds =
                allCategories.stream().map(AssetCategory::getIconFileId).filter(Objects::nonNull).toList();
        Map<Long, FileResponse> fileMap =
                fileService.getFiles(fileIds).stream().collect(Collectors.toMap(FileResponse::id, f -> f));
        List<AssetCategoryResponse> allCategoryDtoList =
                allCategories.stream().map(v -> createAssetCategoryResponse(v, fileMap)).toList();
        return AssetCategoryAllResponse.of(
                AssetCategory.builder().build().getMaxDepth(), makeCategoryList(allCategoryDtoList));
    }

    private List<AssetCategoryResponse> makeCategoryList(List<AssetCategoryResponse> list) {
        Map<Long, AssetCategoryResponse> categoryMap = new HashMap<>();
        for (AssetCategoryResponse c : list) {
            categoryMap.put(c.id(), c); // list의 객체를 그대로 Map에 넣는다
        }
        List<AssetCategoryResponse> roots = new ArrayList<>();

        for (AssetCategoryResponse category : list) {
            Long parentId = category.parentId();
            if (parentId == null) {
                // 루트 카테고리
                roots.add(category);
            } else {
                // 부모를 찾아서 children에 추가
                AssetCategoryResponse findParent = categoryMap.get(parentId);
                findParent.children().add(category); // flatList의 객체 그대로 추가
            }
        }
        return roots; // 루트부터 시작하는 트리 반환
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryResponse> getChildCategories(Long parentId) {
        List<AssetCategory> childCategories = assetCategoryRepository.findByParentId(parentId);
        return childCategories.stream().map(this::createAssetCategoryResponseWithoutChildren).toList();
    }

    private AssetCategoryResponse createAssetCategoryResponse(
            AssetCategory category, Map<Long, FileResponse> fileMap) {
        return AssetCategoryResponse.from(
                category, category.getIconFileId() != null ? fileMap.get(category.getIconFileId()) : null);
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

        if (request.thumbnailFileId() != null
                && !request.thumbnailFileId().equals(category.getIconFileId())) {
            category.updateIconFileId(request.thumbnailFileId());
        }

        if (request.parentId() == null) {
            category.assignToParent(null);
            return;
        }

        AssetCategory parent = findById(request.parentId());
        if (parent.getId().equals(category.getId())) {
            throw new CustomException(INVALID_PARENT_CATEGORY);
        }
        category.assignToParent(parent);
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
