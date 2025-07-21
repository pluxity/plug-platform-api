package com.pluxity.station.dto;

import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import java.util.List;
import lombok.Builder;

public record StationResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        List<String> stationCodes) {

    @Builder
    public StationResponse(
            FacilityResponse facility,
            List<FloorResponse> floors,
            List<Long> lineIds,
            List<String> stationCodes) {
        this.facility = facility;
        this.floors = floors != null ? floors : List.of();
        this.lineIds = lineIds != null ? lineIds : List.of();
        this.stationCodes = stationCodes != null ? stationCodes : List.of();
    }
}
