package com.pluxity.facility.dto;

import java.util.List;

public record FacilityAllResponse(
        List<FacilityResponse> building,
        List<FacilityResponse> station,
        List<FacilityResponse> panorama) {
    public static FacilityAllResponse from(
            List<FacilityResponse> building,
            List<FacilityResponse> station,
            List<FacilityResponse> panorama) {
        return new FacilityAllResponse(building, station, panorama);
    }
}
