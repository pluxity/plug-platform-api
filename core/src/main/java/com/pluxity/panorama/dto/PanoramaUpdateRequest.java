package com.pluxity.panorama.dto;

public record PanoramaUpdateRequest(
        String name, String description, Long drawingFileId, Long thumbnailFileId) {}
