package com.pluxity.domains.station.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.station.dto.StationResponse;

public record SasangStationResponse(
        @JsonUnwrapped StationResponse stationResponse, String externalCode) {

    public static SasangStationResponse of(StationResponse stationResponse, String externalCode) {
        return new SasangStationResponse(stationResponse, externalCode);
    }
}
