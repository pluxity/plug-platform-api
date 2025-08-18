package com.pluxity.facility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FacilityType {
    BUILDING("buildings", "건물"),
    STATION("stations", "역"),
    PARK("parks", "공원");

    private final String key;
    private final String description;
}
