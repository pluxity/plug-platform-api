package com.pluxity.panorama.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record PanoramaResponse(
        FacilityResponse facility, @JsonUnwrapped BaseResponse baseResponse) {}
