package com.pluxity.facility.dto;

public record PanoramaUpdateRequest(
        LocationRequest locationRequest,
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId
) {
}
