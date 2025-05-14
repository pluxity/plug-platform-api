package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record BuildingResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        @JsonUnwrapped BaseResponse baseResponse) {}
