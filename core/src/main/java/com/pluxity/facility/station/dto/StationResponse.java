package com.pluxity.facility.station.dto;

import com.pluxity.facility.facility.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record StationResponse(
        FacilityResponse facility, List<FloorResponse> floors, Long lineId, String route) {}
