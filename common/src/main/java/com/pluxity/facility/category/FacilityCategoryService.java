package com.pluxity.facility.category;

import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest;
import com.pluxity.facility.category.dto.FacilityCategoryResponse;
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityCategoryService {
    private final FacilityCategoryRepository repository;

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
                            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "부모 카테고리를 찾을 수 없습니다."));
        }

        FacilityCategory entity =
                FacilityCategory.builder().name(request.name()).parent(parent).build();
        repository.save(entity);

        return FacilityCategoryResponse.from(entity);
    }

    @Transactional(readOnly = true)
    public List<FacilityCategoryResponse> findAll() {
        return repository.findAll().stream()
                .map(FacilityCategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacilityCategoryResponse findById(Long id) {
        FacilityCategory entity =
                repository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
        return FacilityCategoryResponse.from(entity);
    }

    @Transactional
    public void update(Long id, FacilityCategoryUpdateRequest request) {
        FacilityCategory category =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Category not found", HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));

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
                                            new CustomException(
                                                    "Parent not found", HttpStatus.NOT_FOUND, "부모 카테고리를 찾을 수 없습니다."));

            category.assignToParent(parent);
        }

        repository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        FacilityCategory facility =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "FacilityCategory not found", HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));

        if (!facility.getChildren().isEmpty()) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED, "하위 카테고리가 있어 삭제할 수 없습니다.");
        }

        repository.delete(facility);
    }
}
