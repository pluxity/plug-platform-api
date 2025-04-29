package com.pluxity.facility.dto;

import jakarta.validation.constraints.NotBlank;

public record FacilityUpdateRequest(
    @NotBlank(message = "건물 이름은 필수입니다.")
    String name,
    String description,
    Long drawingFileId,
    Long thumbnailFileId
) {
    public static FacilityUpdateRequest of(String name, String description, Long drawingFileId, Long thumbnailFileId) {
        return new FacilityUpdateRequest(name, description, drawingFileId, thumbnailFileId);
    }
} 