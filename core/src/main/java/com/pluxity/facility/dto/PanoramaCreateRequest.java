package com.pluxity.facility.dto;

public record PanoramaCreateRequest(
        FacilityCreateRequest facility,
        LocationRequest locationRequest,
        String address,
        Long drawingFileId,
        Long thumbnailFileId) {}
