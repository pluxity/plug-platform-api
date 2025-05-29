package com.pluxity.facility.station.dto;

import com.pluxity.facility.facility.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import lombok.Builder;

import java.util.List;


public record StationResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        String route) {

    @Builder
    public StationResponse(
            FacilityResponse facility, List<FloorResponse> floors, List<Long> lineIds, String route) {
        this.facility = facility;
        this.floors = floors != null ? floors : List.of();
        this.lineIds = lineIds != null ? lineIds : List.of();
        this.route = route;
    }
}
