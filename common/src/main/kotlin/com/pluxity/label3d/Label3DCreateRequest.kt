package com.pluxity.label3d

import com.pluxity.feature.entity.Spatial
import jakarta.validation.constraints.NotBlank

data class Label3DCreateRequest(
    @field:NotBlank
    val id: String,
    val displayText: String?,
    @field:NotBlank
    val facilityId: Long,
    val floorId: String?,
    val position: Spatial?,
    val rotation: Spatial?,
    val scale: Spatial?,
)
