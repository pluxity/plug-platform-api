package com.pluxity.facility.dto;

import com.pluxity.facility.entity.FacilityCategory;

public record FacilityCategoryResponse(Long id, String name, Long parentId) {
    public static FacilityCategoryResponse from(FacilityCategory category) {
        return new FacilityCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null);
    }
}
