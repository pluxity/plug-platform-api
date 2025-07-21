package com.pluxity.building.dto;

import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record BuildingResponse(FacilityResponse facility, List<FloorResponse> floors) {}
