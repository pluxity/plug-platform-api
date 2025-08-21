package com.pluxity.facility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FacilityType {
    BUILDING("건물"),
    STATION("역"),
    PARK("공원");

    private final String description;
}
