package com.pluxity.domains.sasang.station.dto;

import com.pluxity.domains.sasang.station.enums.BusanSubwayStation; // Corrected enum import

public record BusanSubwayStationResponse(String code, String name, String line) {

    public static BusanSubwayStationResponse from(BusanSubwayStation station) {
        return new BusanSubwayStationResponse(station.getCode(), station.getName(), station.getLine());
    }
}
