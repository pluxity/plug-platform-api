package com.pluxity.label3d

import com.pluxity.feature.entity.Spatial

data class Label3DUpdateRequest(
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
)
