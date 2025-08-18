package com.pluxity.cctv.dto

import com.pluxity.device.dto.DeviceInfoResponse

data class CctvInfoResponse(
    val id: String,
    val name: String,
    val url: String?,
    val type: String,
) : DeviceInfoResponse
