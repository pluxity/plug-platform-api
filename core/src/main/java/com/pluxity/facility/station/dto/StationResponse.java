package com.pluxity.facility.station.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.facility.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import java.util.List;
import lombok.Builder;

public record StationResponse(
        @JsonUnwrapped FacilityResponse facility,
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
