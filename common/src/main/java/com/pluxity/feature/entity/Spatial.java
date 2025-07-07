package com.pluxity.feature.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spatial {

    private Double x;

    private Double y;

    private Double z;

    @Builder
    public Spatial(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
