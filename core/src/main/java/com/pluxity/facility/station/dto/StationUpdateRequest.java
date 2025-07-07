package com.pluxity.facility.station.dto;

import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import java.util.List;

public record StationUpdateRequest(
        FacilityUpdateRequest facility, List<FloorRequest> floors, List<Long> lineIds) {}
