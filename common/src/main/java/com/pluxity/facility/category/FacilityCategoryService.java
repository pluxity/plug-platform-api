package com.pluxity.facility.category;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY_CATEGORY;
import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY_PARENT_CATEGORY;

import com.pluxity.facility.category.dto.FacilityCategoryAllResponse;
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest;
import com.pluxity.facility.category.dto.FacilityCategoryResponse;
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FacilityMappingUtils;
import com.pluxity.global.utils.SortUtils;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityCategoryService {
    private final FacilityCategoryRepository repository;
    private final FileService fileService;

    @Transactional
    public FacilityCategoryResponse create(FacilityCategoryCreateRequest request) {
        FacilityCategory parent = null;
        if (request.parentId() != null) {
            repository
                    .findByNameAndParentId(request.name(), request.parentId())
                    .ifPresent(
                            existingCategory -> {
                                throw new CustomException(ErrorCode.INVALID_REFERENCE, "이미 존재하는 카테고리 이름입니다.");
                            });

            parent =
                    repository
                            .findById(request.parentId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(
                                                    ErrorCode.NOT_FOUND_FACILITY_PARENT_CATEGORY, request.parentId()));
        }

        FacilityCategory entity =
                FacilityCategory.builder().name(request.name()).parent(parent).build();
        repository.save(entity);

        return FacilityCategoryResponse.from(entity);
    }

    @Transactional(readOnly = true)
    public FacilityCategoryAllResponse findAll() {
        List<FacilityCategory> allCategories = repository.findAll(SortUtils.getOrderByCreatedAtDesc());
        List<Long> fileIds =
                allCategories.stream()
                        .map(FacilityCategory::getImageFileId)
                        .filter(Objects::nonNull)
                        .toList();
        Map<Long, FileResponse> fileMap = FacilityMappingUtils.getFileMapByIds(fileIds, fileService);
        List<FacilityCategoryResponse> list =
                allCategories.stream()
                        .map(v -> FacilityCategoryResponse.from(v, fileMap.get(v.getImageFileId())))
                        .collect(Collectors.toList());
        return FacilityCategoryAllResponse.of(
                FacilityCategory.builder().build().getMaxDepth(), makeCategoryList(list));
    }

    private List<FacilityCategoryResponse> makeCategoryList(List<FacilityCategoryResponse> list) {
        Map<Long, FacilityCategoryResponse> categoryMap = new HashMap<>();
        for (FacilityCategoryResponse c : list) {
            categoryMap.put(c.id(), c); // list의 객체를 그대로 Map에 넣는다
        }
        List<FacilityCategoryResponse> roots = new ArrayList<>();

        for (FacilityCategoryResponse category : list) {
            Long parentId = category.parentId();
            if (parentId == null) {
                // 루트 카테고리
                roots.add(category);
            } else {
                // 부모를 찾아서 children에 추가
                FacilityCategoryResponse findParent = categoryMap.get(parentId);
                findParent.children().add(category); // list의 객체 그대로 추가
            }
        }
        return roots; // 루트부터 시작하는 트리 반환
    }

    @Transactional(readOnly = true)
    public FacilityCategoryResponse findById(Long id) {
        FacilityCategory entity =
                repository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY_CATEGORY, id));
        return FacilityCategoryResponse.from(entity);
    }

    @Transactional
    public void update(Long id, FacilityCategoryUpdateRequest request) {
        FacilityCategory category =
                repository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY_CATEGORY, id));

        if (request.name() != null) category.setName(request.name());
        if (request.parentId() != null) {

            if (request.parentId().equals(id)) {
                throw new CustomException(ErrorCode.INVALID_REFERENCE, "자기 자신을 부모로 설정할 수 없습니다.");
            }
            FacilityCategory parent =
                    repository
                            .findById(request.parentId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(NOT_FOUND_FACILITY_PARENT_CATEGORY, request.parentId()));

            category.assignToParent(parent);
        }

        repository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        FacilityCategory facility =
                repository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY_CATEGORY, id));

        if (!facility.getChildren().isEmpty()) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED, "하위 카테고리가 있어 삭제할 수 없습니다.");
        }

        repository.delete(facility);
    }
}
