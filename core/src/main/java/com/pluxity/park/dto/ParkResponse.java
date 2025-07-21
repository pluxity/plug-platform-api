package com.pluxity.park.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record ParkResponse(
        FacilityResponse facility, String boundary, @JsonUnwrapped BaseResponse baseResponse) {}
