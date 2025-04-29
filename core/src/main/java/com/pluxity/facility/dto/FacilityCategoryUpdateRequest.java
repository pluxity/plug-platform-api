package com.pluxity.facility.dto;

public record FacilityCategoryUpdateRequest(
        String name,
        Long parentId
) {
}
