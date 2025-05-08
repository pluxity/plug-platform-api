package com.pluxity.facility.dto;

public record PanoramaCreateRequest(
        FacilityCreateRequest facility,
        String address,
        Double latitude,
        Double longitude,
        Long drawingFileId,
        Long thumbnailFileId
) {
}
