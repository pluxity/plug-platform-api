package com.pluxity.feature.dto

import com.pluxity.feature.entity.Spatial
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FeatureCreateRequest(
    // UUID
    @field:NotBlank
    val id: String,
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
    @field:NotNull
    var assetId: Long,
    @field:NotNull
    var facilityId: Long,
    val floorId: String?,
)
