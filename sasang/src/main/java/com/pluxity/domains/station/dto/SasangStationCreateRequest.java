package com.pluxity.domains.station.dto;

import com.pluxity.facility.station.dto.StationCreateRequest;

public record SasangStationCreateRequest(
        StationCreateRequest stationRequest, String code, String externalCode) {}
