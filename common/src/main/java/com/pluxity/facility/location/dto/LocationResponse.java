package com.pluxity.facility.location.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.location.Location;
import com.pluxity.facility.panorama.Panorama;
import com.pluxity.global.response.BaseResponse;

public record LocationResponse(
        Double latitude, Double longitude, Double altitude, @JsonUnwrapped BaseResponse baseResponse) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                BaseResponse.of(location));
    }

    public static LocationResponse from(Panorama panorama) {
        return new LocationResponse(
                panorama.getLatitude(),
                panorama.getLongitude(),
                panorama.getAltitude(),
                BaseResponse.of(panorama));
    }
}
