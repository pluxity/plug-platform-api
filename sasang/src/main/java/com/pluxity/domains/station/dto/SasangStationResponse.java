package com.pluxity.domains.station.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.station.dto.StationResponse;
import lombok.Builder;

@Builder
public record SasangStationResponse(
        @JsonUnwrapped StationResponse stationResponse, String code, String externalCode) {}
