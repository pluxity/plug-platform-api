package com.pluxity.cctv;

import com.pluxity.cctv.category.CctvCategory;
import com.pluxity.cctv.category.CctvCategoryService;
import com.pluxity.cctv.category.dto.CctvCategoryResponse;
import com.pluxity.cctv.dto.CctvCreateRequest;
import com.pluxity.cctv.dto.CctvResponse;
import com.pluxity.cctv.dto.CctvUpdateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.FeatureType;
import com.pluxity.feature.service.FeatureFacade;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CctvService {

    private final CctvRepository cctvRepository;
    private final CctvCategoryService cctvCategoryService;
    private final FeatureFacade featureFacade;

    @Transactional
    public String create(@Valid CctvCreateRequest request) {
        CctvCategory category =
                MappingUtils.findByIdIfExists(request.categoryId(), cctvCategoryService::findById);
        return cctvRepository
                .save(
                        Cctv.builder()
                                .id(request.id())
                                .name(request.name())
                                .url(request.url())
                                .category(category)
                                .build())
                .getId();
    }

    @Transactional(readOnly = true)
    public List<CctvResponse> findAll() {
        List<Cctv> list = cctvRepository.findAll();
        return list.stream().map(this::createResponse).toList();
    }

    @Transactional(readOnly = true)
    public CctvResponse getById(String id) {
        return createResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public CctvResponse getByFeatureId(String featureId) {
        Feature feature = featureFacade.findById(featureId);
        Cctv cctv =
                cctvRepository
                        .findByFeature(feature)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CCTV_BY_FEATURE, featureId));
        return createResponse(cctv);
    }

    private CctvResponse createResponse(Cctv cctv) {
        return CctvResponse.builder()
                .id(cctv.getId())
                .name(cctv.getName())
                .url(cctv.getUrl())
                .feature(cctv.getFeature() != null ? FeatureResponse.from(cctv.getFeature()) : null)
                .cctvCategory(
                        cctv.getCategory() != null ? CctvCategoryResponse.from(cctv.getCategory()) : null)
                .build();
    }

    @Transactional
    public void update(String id, CctvUpdateRequest request) {
        Cctv cctv = findById(id);
        cctv.updateCctv(request);
        CctvCategory category = cctvCategoryService.findById(request.categoryId());
        cctv.changeCategory(category);
    }

    @Transactional
    public void delete(String id) {
        Cctv cctv = findById(id);
        Feature feature = cctv.getFeature();
        feature.updateFeatureType(FeatureType.NONE);
        cctvRepository.delete(cctv);
    }

    @Transactional(readOnly = true)
    public Cctv findById(String id) {
        return cctvRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CCTV, id));
    }
}
