package com.pluxity.climate.dto

data class ClimateDeviceCreateRequest(
    val id: String,
    val name: String,
    val categoryId: Long?,
)
