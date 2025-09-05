package com.pluxity.facility.floor.dto

import jakarta.validation.constraints.NotBlank

data class FloorRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val floorId: String,
)
