package com.pluxity.feature.dto

import com.pluxity.feature.entity.Spatial

data class FeatureUpdateRequest(
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
)
