package com.pluxity.facility.building.dto;

import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
// import com.pluxity.global.response.BaseResponse; // Removed
import java.util.List;
// import lombok.Builder; // Removed

// @Builder // Removed
public record BuildingResponse(
    Long id, // Added Building's own ID
    FacilityResponse facility,
    List<FloorResponse> floors
    // @JsonUnwrapped BaseResponse baseResponse // Removed
) {}
