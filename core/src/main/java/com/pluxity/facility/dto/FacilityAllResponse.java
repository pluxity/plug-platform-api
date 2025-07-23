package com.pluxity.facility.dto;

import java.util.List;

public record FacilityAllResponse(
        List<FacilityResponse> buildings, List<FacilityResponse> stations) {
    public static FacilityAllResponse from(
            List<FacilityResponse> building, List<FacilityResponse> station) {
        return new FacilityAllResponse(building, station);
    }
}
