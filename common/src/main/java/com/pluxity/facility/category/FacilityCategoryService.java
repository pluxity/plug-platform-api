package com.pluxity.facility.category;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY_CATEGORY;
import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY_PARENT_CATEGORY;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pluxity.facility.Facility;
import com.pluxity.facility.category.dto.FacilityCategoryAllResponse;
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest;
import com.pluxity.facility.category.dto.FacilityCategoryResponse;
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.annotation.CheckPermissionCategory;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.global.utils.SortUtils;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.service.AclService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityCategoryService {
    private final FacilityCategoryRepository repository;
    private final FileService fileService;
    private final AclService aclService;

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
        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(
                        allCategories, v -> Stream.of(v.getImageFileId()), fileService);
        List<FacilityCategoryResponse> list =
                allCategories.stream()
                        .map(v -> FacilityCategoryResponse.from(v, fileMap.get(v.getImageFileId())))
                        .collect(Collectors.toList());

        return FacilityCategoryAllResponse.of(
                FacilityCategory.builder().build().getMaxDepth(),
                MappingUtils.makeCategoryTree(
                        list,
                        FacilityCategoryResponse::id,
                        FacilityCategoryResponse::parentId,
                        FacilityCategoryResponse::children));
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

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.FACILITY_CATEGORY)
    public List<FacilityCategory> findAllTest() {
        List<FacilityCategory> all = repository.findAll();
        return all;
    }

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.FACILITY_CATEGORY)
    public FacilityCategory findByTest(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RESOURCE));
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // 자식 노드가 없을 때 "children": [] 생략
    public static class FacilityResponse {
        private Long id;
        private String name;
        private String code;
        private String categoryName;

        public static FacilityResponse from(Facility facility) {
            return FacilityResponse.builder()
                    .id(facility.getId())
                    .name(facility.getName())
                    .code(facility.getCode())
                    .categoryName(facility.getCategory() != null ? facility.getCategory().getName() : null)
                    .build();
        }
    }

    public List<FacilityResponse> getFacilityWithGrouping() {
        List<FacilityCategory> facilityCategories = aclService.facilityCategories();

        return facilityCategories.stream()
                .flatMap(category -> category.getFacilities().stream())
                .map(FacilityResponse::from)
                .collect(Collectors.toList());
    }
}
