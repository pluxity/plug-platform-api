package com.pluxity.facility.facility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FacilityType {
    BUILDING("건물"),
    STATION("역사"),
    PARK("공원"),
    BRIDGE("교량"),
    TUNNEL("터널"),
    PANORAMA("파노라마");

    private final String displayName;
} 