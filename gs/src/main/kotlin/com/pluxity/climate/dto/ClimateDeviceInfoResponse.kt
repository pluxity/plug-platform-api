package com.pluxity.climate.dto

import com.pluxity.device.dto.DeviceInfoResponse

data class ClimateDeviceInfoResponse(
    val id: String,
    val name: String,
    val type: String,
    val featureId: String?,
) : DeviceInfoResponse
