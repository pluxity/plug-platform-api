package com.pluxity.park.dto;

import com.pluxity.facility.dto.FacilityUpdateRequest;

public record ParkUpdateRequest(FacilityUpdateRequest facility, String boundary) {}
