package com.pluxity.feature.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeatureType {
    NONE("미설정"),
    DEVICE("디바이스"),
    LABEL("Label3D"),
    CCTV("CCTV");

    private final String description;
}
