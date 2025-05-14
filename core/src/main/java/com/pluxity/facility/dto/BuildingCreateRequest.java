package com.pluxity.facility.dto;

import java.util.List;

public record BuildingCreateRequest(FacilityCreateRequest facility, List<FloorRequest> floors) {}
