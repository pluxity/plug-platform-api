package com.pluxity.domains.station.dto;

import com.pluxity.facility.station.dto.StationUpdateRequest;

public record SasangStationUpdateRequest(
        StationUpdateRequest stationUpdateRequest, String externalCode) {}
