package com.pluxity.label3d

import com.pluxity.feature.entity.Spatial

data class Label3DUpdateRequest(
    val position: Spatial? = null,
    val rotation: Spatial? = null,
    val scale: Spatial? = null,
)
