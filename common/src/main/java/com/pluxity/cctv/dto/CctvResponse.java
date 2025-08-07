package com.pluxity.cctv.dto;

import com.pluxity.cctv.category.dto.CctvCategoryResponse;
import com.pluxity.feature.dto.FeatureResponse;
import lombok.Builder;

@Builder
public record CctvResponse(
        String id,
        String name,
        String url,
        FeatureResponse feature,
        CctvCategoryResponse cctvCategory) {}
