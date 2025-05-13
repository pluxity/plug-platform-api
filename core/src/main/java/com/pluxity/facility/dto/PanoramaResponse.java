package com.pluxity.facility.dto;

import lombok.Builder;

@Builder
public record PanoramaResponse(FacilityResponse facility, LocationResponse location) {}
