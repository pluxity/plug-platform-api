package com.pluxity.domains.station.dto;

public record AdjacentStationResponse(String code, String name) {
    public static AdjacentStationResponse of(String code, String name) {
        return new AdjacentStationResponse(code, name);
    }
}
