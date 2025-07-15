package com.pluxity.building.dto;

import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import java.util.List;

public record BuildingUpdateRequest(FacilityUpdateRequest facility, List<FloorRequest> floors) {}
