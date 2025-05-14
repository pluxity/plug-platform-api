package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record BuildingResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        @JsonUnwrapped BaseResponse baseResponse) {}
