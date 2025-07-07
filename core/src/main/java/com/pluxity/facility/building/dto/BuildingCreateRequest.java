package com.pluxity.facility.building.dto;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import java.util.List;

public record BuildingCreateRequest(FacilityCreateRequest facility, List<FloorRequest> floors) {}
