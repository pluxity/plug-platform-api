package com.pluxity.facility.station.dto;

import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.feature.dto.FeatureResponse;
import java.util.List;
import lombok.Builder;

public record StationResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        List<FeatureResponse> features,
        String route) {

    @Builder
    public StationResponse(
            FacilityResponse facility,
            List<FloorResponse> floors,
            List<Long> lineIds,
            List<FeatureResponse> features,
            String route) {
        this.facility = facility;
        this.floors = floors != null ? floors : List.of();
        this.lineIds = lineIds != null ? lineIds : List.of();
        this.features = features != null ? features : List.of();
        this.route = route;
    }
}
