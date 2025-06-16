package com.pluxity.label3d;

import com.pluxity.feature.entity.Spatial;

public record Label3DUpdateRequest(Spatial position, Spatial rotation, Spatial scale) {}
