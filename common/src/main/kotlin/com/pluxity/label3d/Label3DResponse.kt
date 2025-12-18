package com.pluxity.label3d

import com.pluxity.feature.entity.Spatial

data class Label3DResponse(
    val id: String,
    val displayText: String,
    val floorId: String?,
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
)

fun Label3D.toLabel3DResponse(): Label3DResponse =
    Label3DResponse(
        id = this.requiredId(),
        displayText = requireNotNull(this.displayText) { "displayText is null (not ready)" },
        floorId = this.feature.floorId,
        position = this.feature.position,
        rotation = this.feature.rotation,
        scale = this.feature.scale,
    )
