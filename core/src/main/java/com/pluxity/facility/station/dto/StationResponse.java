package com.pluxity.facility.station.dto;

import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import java.util.List;
import lombok.Builder;

public record StationResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        List<String> featureIds,
        String route,
        String subway) {

    @Builder
    public StationResponse(
            FacilityResponse facility,
            List<FloorResponse> floors,
            List<Long> lineIds,
            List<String> featureIds,
            String route,
            String subway) {
        this.facility = facility;
        this.floors = floors != null ? floors : List.of();
        this.lineIds = lineIds != null ? lineIds : List.of();
        this.featureIds = featureIds != null ? featureIds : List.of();
        this.route = route;
        this.subway = subway;
    }
}
