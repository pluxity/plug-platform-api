package com.pluxity.facility.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record BuildingResponse(FacilityResponse facility, List<FloorResponse> floors) {}
