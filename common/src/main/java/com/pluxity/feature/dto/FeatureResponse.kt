package com.pluxity.feature.dto

import com.pluxity.feature.entity.Feature
import com.pluxity.feature.entity.Spatial

data class FeatureResponse(
    val id: String,
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
    val assetId: Long?,
    val floorId: String?,
)

fun Feature.toFeatureResponse() =
    FeatureResponse(
        id = this.id!!,
        position = this.position,
        rotation = this.rotation,
        scale = this.scale,
        assetId = this.assetId,
        floorId = this.floorId,
    )
