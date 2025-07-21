package com.pluxity.park.dto;

import com.pluxity.facility.dto.FacilityCreateRequest;

public record ParkCreateRequest(FacilityCreateRequest facility, String boundary) {}
