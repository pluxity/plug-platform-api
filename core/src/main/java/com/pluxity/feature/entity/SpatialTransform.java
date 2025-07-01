package com.pluxity.feature.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SpatialTransform {

    private static final SpatialTransform DEFAULT_TRANSFORM = SpatialTransform.builder()
            .position(Spatial.zero())
            .rotation(Spatial.zero())
            .scale(Spatial.one())
            .build();

    private Spatial position;
    private Spatial rotation;
    private Spatial scale;

    public static SpatialTransform createDefault() {
        return DEFAULT_TRANSFORM;
    }

    public static SpatialTransform create(Spatial position, Spatial rotation, Spatial scale) {
        return SpatialTransform.builder()
                .position(position != null ? position : Spatial.zero())
                .rotation(rotation != null ? rotation : Spatial.zero())
                .scale(scale != null ? scale : Spatial.one())
                .build();
    }

    public SpatialTransform updatePosition(Spatial newPosition) {
        if (newPosition == null) return this;
        return SpatialTransform.builder()
                .position(newPosition)
                .rotation(this.rotation)
                .scale(this.scale)
                .build();
    }

    public SpatialTransform updateRotation(Spatial newRotation) {
        if (newRotation == null) return this;
        return SpatialTransform.builder()
                .position(this.position)
                .rotation(newRotation)
                .scale(this.scale)
                .build();
    }

    public SpatialTransform updateScale(Spatial newScale) {
        if (newScale == null) return this;
        return SpatialTransform.builder()
                .position(this.position)
                .rotation(this.rotation)
                .scale(newScale)
                .build();
    }

    public boolean isDefaultPosition() {
        return position.equals(Spatial.zero());
    }

    public boolean isDefaultRotation() {
        return rotation.equals(Spatial.zero());
    }

    public boolean isDefaultScale() {
        return scale.equals(Spatial.one());
    }

    public boolean isCompletelyDefault() {
        return isDefaultPosition() && isDefaultRotation() && isDefaultScale();
    }
} 