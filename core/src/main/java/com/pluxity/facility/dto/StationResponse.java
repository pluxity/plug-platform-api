package com.pluxity.facility.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record StationResponse(FacilityResponse facility, List<FloorResponse> floors) {}
