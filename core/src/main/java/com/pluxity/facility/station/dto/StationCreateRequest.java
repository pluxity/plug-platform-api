package com.pluxity.facility.station.dto;

import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import java.util.List;

public record StationCreateRequest(
        FacilityCreateRequest facility, List<FloorRequest> floors, List<Long> lineIds) {}
