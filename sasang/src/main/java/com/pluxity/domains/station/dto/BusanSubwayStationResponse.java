package com.pluxity.domains.station.dto;

import com.pluxity.domains.station.enums.BusanSubwayStation;

public record BusanSubwayStationResponse(String code, String name, String line) {

    public static BusanSubwayStationResponse from(BusanSubwayStation station) {
        return new BusanSubwayStationResponse(station.getCode(), station.getName(), station.getLine());
    }
}
