package com.pluxity.facility.dto;

import jakarta.validation.constraints.NotBlank;

public record FacilityUpdateRequest(
    @NotBlank(message = "건물 이름은 필수입니다.")
    String name,
    String description,
    Long fileId,
    Long thumbnailId
) {
    public static FacilityUpdateRequest of(String name, String description, Long fileId, Long thumbnailId) {
        return new FacilityUpdateRequest(name, description, fileId, thumbnailId);
    }
} 