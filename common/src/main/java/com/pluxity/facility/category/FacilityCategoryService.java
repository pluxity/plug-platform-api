package com.pluxity.facility.category;

import static com.pluxity.global.constant.ErrorCode.*;
import static com.pluxity.global.constant.ErrorCode.INVALID_REFERENCE;

import com.pluxity.category.service.CategoryService;
import com.pluxity.facility.category.dto.FacilityCategoryAllResponse;
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest;
import com.pluxity.facility.category.dto.FacilityCategoryResponse;
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.global.utils.SortUtils;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityCategoryService extends CategoryService<FacilityCategory> {
    private final FacilityCategoryRepository repository;

    @Override
    protected JpaRepository<FacilityCategory, Long> getRepository() {
        return repository;
    }

    @Transactional
    public Long create(FacilityCategoryCreateRequest request) {
        if (request.parentId() != null) {
            repository
                    .findByNameAndParentId(request.name(), request.parentId())
                    .ifPresent(
                            existingCategory -> {
                                throw new CustomException(INVALID_REFERENCE, request.name());
                            });
        }
        FacilityCategory entity = FacilityCategory.builder().name(request.name()).build();
        FacilityCategory parent = MappingUtils.findByIdIfExists(request.parentId(), super::findById);

        return super.create(entity, parent);
    }

    @Transactional(readOnly = true)
    public FacilityCategoryAllResponse findAll() {
        List<FacilityCategoryResponse> list =
                repository.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
                        .map(FacilityCategoryResponse::from)
                        .collect(Collectors.toList());

        return FacilityCategoryAllResponse.of(
                FacilityCategory.builder().build().getMaxDepth(),
                MappingUtils.makeCategoryTree(
                        list,
                        FacilityCategoryResponse::id,
                        FacilityCategoryResponse::parentId,
                        FacilityCategoryResponse::children));
    }

    @Transactional
    public void update(Long id, FacilityCategoryUpdateRequest request) {
        super.update(id, request.name(), request.parentId());
    }

    @Transactional
    public void delete(Long id) {
        FacilityCategory facility = findById(id);

        if (!facility.getChildren().isEmpty()) {
            throw new CustomException(CATEGORY_HAS_CHILDREN);
        }

        if (!facility.getFacilities().isEmpty()) {
            throw new CustomException(FACILITY_CATEGORY_HAS_FACILITY);
        }

        repository.delete(facility);
    }
}
