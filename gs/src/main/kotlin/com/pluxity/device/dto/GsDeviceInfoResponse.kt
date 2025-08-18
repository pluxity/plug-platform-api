package com.pluxity.device.dto

data class GsDeviceInfoResponse(
    val id: String,
    val name: String,
    val type: String,
) : DeviceInfoResponse
