package com.pluxity.facility.floor.dto

import jakarta.validation.constraints.NotBlank

@JvmRecord
data class FloorRequest(val name: @NotBlank String?, val floorId: @NotBlank String?)
