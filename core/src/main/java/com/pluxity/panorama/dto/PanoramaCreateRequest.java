package com.pluxity.panorama.dto;

import com.pluxity.facility.dto.FacilityCreateRequest;

public record PanoramaCreateRequest(
        FacilityCreateRequest facility, String address, Long drawingFileId, Long thumbnailFileId) {}
