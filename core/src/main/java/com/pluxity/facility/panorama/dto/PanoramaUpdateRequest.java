package com.pluxity.facility.panorama.dto;

import com.pluxity.facility.location.dto.LocationRequest;

public record PanoramaUpdateRequest(
        LocationRequest locationRequest,
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId) {}
