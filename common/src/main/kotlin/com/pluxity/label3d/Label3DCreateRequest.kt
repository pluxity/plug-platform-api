package com.pluxity.label3d

import com.pluxity.feature.entity.Spatial
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class Label3DCreateRequest(
    @field:NotBlank
    val id: String,
    val displayText: String?,
    @field:NotNull
    var facilityId: Long,
    val floorId: String?,
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
)
