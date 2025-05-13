package com.pluxity.facility.dto;

public record StationUpdateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId
) {
}
