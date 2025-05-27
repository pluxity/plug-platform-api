package com.pluxity.domains.station.dto;

import com.pluxity.facility.station.dto.StationResponse;
import lombok.Builder;

@Builder
public record SasangStationResponse(
        StationResponse stationResponse, String code, String externalCode) {}
