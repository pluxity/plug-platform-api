package com.pluxity.facility.panorama.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.facility.FacilityResponse;
import com.pluxity.facility.location.dto.LocationResponse;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record PanoramaResponse(
        FacilityResponse facility,
        LocationResponse location,
        @JsonUnwrapped BaseResponse baseResponse) {}
