package com.pluxity.cctv.category;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_CCTV_CATEGORY;

import com.pluxity.category.service.CategoryService;
import com.pluxity.cctv.category.dto.CctvCategoryAllResponse;
import com.pluxity.cctv.category.dto.CctvCategoryCreateRequest;
import com.pluxity.cctv.category.dto.CctvCategoryResponse;
import com.pluxity.cctv.category.dto.CctvCategoryUpdateRequest;
import com.pluxity.global.constant.ErrorCode;
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
public class CctvCategoryService extends CategoryService<CctvCategory> {

    private final CctvCategoryRepository cctvCategoryRepository;

    @Override
    protected JpaRepository<CctvCategory, Long> getRepository() {
        return cctvCategoryRepository;
    }

    @Transactional
    public Long create(CctvCategoryCreateRequest request) {
        CctvCategory saveCategory = CctvCategory.builder().name(request.name()).build();
        CctvCategory parent =
                MappingUtils.getParentCategoryIfExists(request.parentId(), super::findById);
        return super.create(saveCategory, parent);
    }

    @Transactional(readOnly = true)
    public CctvCategoryAllResponse findAll() {
        List<CctvCategoryResponse> list =
                cctvCategoryRepository.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
                        .map(CctvCategoryResponse::from)
                        .collect(Collectors.toList());
        return CctvCategoryAllResponse.of(
                CctvCategory.builder().build().getMaxDepth(),
                MappingUtils.makeCategoryTree(
                        list,
                        CctvCategoryResponse::id,
                        CctvCategoryResponse::parentId,
                        CctvCategoryResponse::children));
    }

    @Transactional(readOnly = true)
    public CctvCategoryResponse getCctvCategory(Long id) {
        CctvCategory category =
                cctvCategoryRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(NOT_FOUND_CCTV_CATEGORY, id));
        return CctvCategoryResponse.from(category);
    }

    @Transactional
    public void update(Long id, CctvCategoryUpdateRequest request) {
        cctvCategoryRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_CCTV_CATEGORY, id));
        super.update(id, request.name(), request.parentId());
    }

    @Transactional
    public void delete(Long id) {
        CctvCategory cctvCategory =
                cctvCategoryRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(NOT_FOUND_CCTV_CATEGORY, id));

        if (!cctvCategory.getChildren().isEmpty()) {
            throw new CustomException(ErrorCode.CCTV_CATEGORY_HAS_CHILDREN);
        }

        if (!cctvCategory.getCctvs().isEmpty()) {
            throw new CustomException(ErrorCode.CCTV_CATEGORY_HAS_CCTV);
        }

        cctvCategoryRepository.delete(cctvCategory);
    }
}
