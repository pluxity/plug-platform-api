package com.pluxity.facility.dto;

import java.util.List;

public record StationCreateRequest(
        FacilityCreateRequest facility, List<FloorRequest> floors, Long lineId) {}
