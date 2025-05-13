package com.pluxity.facility.dto;

import com.pluxity.facility.entity.Location;
import com.pluxity.facility.entity.Panorama;

public record LocationResponse(Double latitude, Double longitude, Double altitude) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    public static LocationResponse from(Panorama panorama) {
        return new LocationResponse(
                panorama.getLatitude(), panorama.getLongitude(), panorama.getAltitude());
    }
}
