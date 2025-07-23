package com.pluxity.station.dto;

import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import java.util.List;

public record StationResponse(
        FacilityResponse facility, List<FloorResponse> floors, StationInfoResponse stationInfo) {

    public static StationResponse of(
            FacilityResponse facility,
            List<FloorResponse> floors,
            List<Long> lineIds,
            List<String> stationCodes) {
        return new StationResponse(
                facility,
                floors != null ? floors : List.of(),
                StationInfoResponse.builder()
                        .lineIds(lineIds != null ? lineIds : List.of())
                        .stationCodes(stationCodes != null ? stationCodes : List.of())
                        .build());
    }
}
