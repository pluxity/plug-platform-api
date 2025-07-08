package com.pluxity.label3d;

import com.pluxity.feature.entity.Spatial;

public record Label3DResponse(
        String id,
        String displayText,
        String floorId,
        Spatial position,
        Spatial rotation,
        Spatial scale) {
    public static Label3DResponse from(Label3D label3D) {
        return new Label3DResponse(
                label3D.getId(),
                label3D.getDisplayText(),
                label3D.getFeature() != null ? label3D.getFeature().getFloorId() : null,
                label3D.getFeature() != null ? label3D.getFeature().getPosition() : null,
                label3D.getFeature() != null ? label3D.getFeature().getRotation() : null,
                label3D.getFeature() != null ? label3D.getFeature().getScale() : null);
    }
}
