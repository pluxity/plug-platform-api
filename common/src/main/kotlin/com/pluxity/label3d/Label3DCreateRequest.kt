package com.pluxity.label3d

import com.pluxity.feature.entity.Spatial

data class Label3DCreateRequest(
    val id: String,
    val displayText: String?,
    val facilityId: Long?,
    val floorId: String?,
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
)
