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
        String route,
        String externalCode,
        AdjacentStationInfo precedingStation,
        AdjacentStationInfo followingStation) {

    @Builder
    public StationResponseWithFeature(
            FacilityResponseWithFeature facility,
            List<FloorResponse> floors,
            List<Long> lineIds,
            List<FeatureResponseWithoutAsset> features,
            String route,
            String externalCode,
            AdjacentStationInfo precedingStation,
            AdjacentStationInfo followingStation) {
        this.facility = facility;
        this.floors = floors != null ? floors : List.of();
        this.lineIds = lineIds != null ? lineIds : List.of();
        this.features = features != null ? features : List.of();
        this.route = route;
        this.externalCode = externalCode;
        this.precedingStation = precedingStation;
        this.followingStation = followingStation;
    }

    public record AdjacentStationInfo(String code, String name) {
        public static AdjacentStationInfo of(String code, String name) {
            return new AdjacentStationInfo(code, name);
        }
    }
}
