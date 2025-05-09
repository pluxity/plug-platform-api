package com.pluxity.facility.dto;

public record FacilityUpdateRequest(
        String name,
        String description,
        Long thumbnailFileId
) {
}
