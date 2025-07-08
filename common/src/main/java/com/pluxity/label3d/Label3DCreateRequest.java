package com.pluxity.label3d;

import com.pluxity.feature.entity.Spatial;

public record Label3DCreateRequest(
        String id,
        String displayText,
        Long facilityId,
        String floorId,
        Spatial position,
        Spatial rotation,
        Spatial scale) {}
