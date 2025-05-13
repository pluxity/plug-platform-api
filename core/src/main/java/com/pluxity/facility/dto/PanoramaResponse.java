package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record PanoramaResponse(
        FacilityResponse facility,
        LocationResponse location,
        @JsonUnwrapped
        BaseResponse baseResponse
) {
}
