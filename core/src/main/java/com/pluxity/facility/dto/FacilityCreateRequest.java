package com.pluxity.facility.dto;

public record FacilityCreateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId
) {
}
