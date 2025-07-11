package com.pluxity.station.dto;

import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.label3d.Label3DResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record StationResponseWithFeature(
        FacilityResponse facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        List<FeatureResponse> features,
        List<Label3DResponse> label3Ds,
        String route,
        String externalCode,
        String subway,
        AdjacentStationInfo precedingStation,
        AdjacentStationInfo followingStation) {

    public StationResponseWithFeature {
        floors = floors != null ? floors : List.of();
        lineIds = lineIds != null ? lineIds : List.of();
        features = features != null ? features : List.of();
        label3Ds = label3Ds != null ? label3Ds : List.of();
    }

    public record AdjacentStationInfo(String code, String name) {
        public static AdjacentStationInfo of(String code, String name) {
            return new AdjacentStationInfo(code, name);
        }
    }
}
