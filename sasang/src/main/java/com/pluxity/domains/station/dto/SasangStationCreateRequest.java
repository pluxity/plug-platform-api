package com.pluxity.domains.station.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.station.dto.StationCreateRequest;

public record SasangStationCreateRequest(
        @JsonUnwrapped StationCreateRequest stationRequest, String externalCode) {}
