package com.pluxity.facility.panorama.dto;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.location.dto.LocationRequest;

public record PanoramaCreateRequest(
        FacilityCreateRequest facility,
        LocationRequest locationRequest,
        String address,
        Long drawingFileId,
        Long thumbnailFileId) {}
