package com.pluxity.feature.dto

import com.pluxity.feature.entity.Spatial
import jakarta.validation.constraints.NotBlank

data class FeatureCreateRequest(
    // UUID
    @field:NotBlank
    val id: String,
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
    val assetId: Long,
    val facilityId: Long,
    val floorId: String?,
)
