package com.pluxity.facility.service;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.entity.FacilityCategory;
import com.pluxity.facility.repository.FacilityCategoryRepository;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityCategoryService {
    private final FacilityCategoryRepository repository;

    @Transactional
    public FacilityCategoryResponse create(FacilityCategoryCreateRequest request) {
        FacilityCategory parent = null;
        if (request.parentId() != null) {
            parent = repository.findById(request.parentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "부모 카테고리를 찾을 수 없습니다."));
        }

        FacilityCategory entity = FacilityCategory.builder()
                .name(request.name())
                .parent(parent)
                .build();
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
        FacilityCategory entity = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
        return FacilityCategoryResponse.from(entity);
    }

    @Transactional
    public void update(Long id, FacilityCategoryUpdateRequest request) {
        FacilityCategory category = repository.findById(id)
                .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));

        if(request.name() != null) category.setName(request.name());
        if(request.parentId() != null) {
            FacilityCategory parent = repository.findById(request.parentId())
                    .orElseThrow(() -> new CustomException("Parent not found", HttpStatus.NOT_FOUND, "부모 카테고리를 찾을 수 없습니다."));

            category.assignToParent(parent);
        }

        repository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        FacilityCategory facility = repository.findById(id)
                .orElseThrow(() -> new CustomException("FacilityCategory not found", HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));

        repository.delete(facility);
    }
}