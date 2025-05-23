package com.pluxity.facility.category.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.category.FacilityCategory;
import com.pluxity.global.response.BaseResponse;

public record FacilityCategoryResponse(
        Long id, String name, Long parentId, @JsonUnwrapped BaseResponse baseResponse) {
    public static FacilityCategoryResponse from(FacilityCategory category) {
        return new FacilityCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                BaseResponse.of(category));
    }
}
