package com.pluxity.domains.station.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.station.dto.StationUpdateRequest;

public record SasangStationUpdateRequest(
        @JsonUnwrapped StationUpdateRequest stationUpdateRequest, String externalCode) {}
