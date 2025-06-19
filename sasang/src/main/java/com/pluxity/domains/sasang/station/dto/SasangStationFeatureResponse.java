package com.pluxity.domains.sasang.station.dto; // Updated package

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.station.dto.StationResponse;

public record SasangStationFeatureResponse(
        @JsonUnwrapped StationResponse stationResponse, String externalCode) {}
