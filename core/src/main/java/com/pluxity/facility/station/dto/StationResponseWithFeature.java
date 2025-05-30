package com.pluxity.facility.station.dto;

import com.pluxity.facility.facility.dto.FacilityResponseWithFeature;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.feature.dto.FeatureResponseWithoutAsset;
import java.util.List;
import lombok.Builder;

public record StationResponseWithFeature(
        FacilityResponseWithFeature facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        List<FeatureResponseWithoutAsset> features,
        String route) {

    @Builder
    public StationResponseWithFeature(
            FacilityResponseWithFeature facility,
            List<FloorResponse> floors,
            List<Long> lineIds,
            List<FeatureResponseWithoutAsset> features,
            String route) {
        this.facility = facility;
        this.floors = floors != null ? floors : List.of();
        this.lineIds = lineIds != null ? lineIds : List.of();
        this.features = features != null ? features : List.of();
        this.route = route;
    }
}
