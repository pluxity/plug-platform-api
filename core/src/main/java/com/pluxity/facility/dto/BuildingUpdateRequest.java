package com.pluxity.facility.dto;

public record BuildingUpdateRequest(
        String name,
        String description,
        Long thumbnailFileId
) {
}
