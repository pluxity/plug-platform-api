package com.pluxity.facility.dto;

public record PanoramaUpdateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId
) {
}
