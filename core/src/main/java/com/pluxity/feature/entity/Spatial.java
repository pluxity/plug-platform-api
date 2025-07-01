package com.pluxity.feature.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spatial {

    private Double x;

    private Double y;

    private Double z;

    @Builder
    public Spatial(Double x, Double y, Double z) {
        this.x = x != null ? x : 0.0;
        this.y = y != null ? y : 0.0;
        this.z = z != null ? z : 0.0;
    }

    public static Spatial zero() {
        return new Spatial(0.0, 0.0, 0.0);
    }

    public static Spatial one() {
        return new Spatial(1.0, 1.0, 1.0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Spatial spatial = (Spatial) obj;
        return Objects.equals(x, spatial.x) &&
               Objects.equals(y, spatial.y) &&
               Objects.equals(z, spatial.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Spatial(x=%.2f, y=%.2f, z=%.2f)", x, y, z);
    }
}
