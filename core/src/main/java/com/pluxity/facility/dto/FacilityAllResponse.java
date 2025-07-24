package com.pluxity.facility.dto;

import java.util.List;

public record FacilityAllResponse(
        List<FacilityResponse> buildings,
        List<FacilityResponse> stations,
        List<FacilityResponse> parks) {
    public static FacilityAllResponse from(
            List<FacilityResponse> buildings,
            List<FacilityResponse> stations,
            List<FacilityResponse> parks) {
        return new FacilityAllResponse(buildings, stations, parks);
    }
}
