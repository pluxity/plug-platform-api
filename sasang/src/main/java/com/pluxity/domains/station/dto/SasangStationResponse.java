package com.pluxity.domains.station.dto;

import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.feature.dto.FeatureResponseWithoutAsset;
import java.util.List;
import lombok.Builder;

@Builder
public record SasangStationResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        List<FeatureResponseWithoutAsset> features,
        String route,
        String externalCode) {

    public static SasangStationResponse of(StationResponse stationResponse, String externalCode) {
        return SasangStationResponse.builder()
                .facility(stationResponse.facility())
                .floors(stationResponse.floors())
                .lineIds(stationResponse.lineIds())
                .features(stationResponse.features())
                .route(stationResponse.route())
                .externalCode(externalCode)
                .build();
    }
}
