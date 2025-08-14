package com.pluxity.device.dto

import com.pluxity.feature.dto.FeatureResponse

data class GsDeviceResponse(
    val id: String,
    val name: String,
    val feature: FeatureResponse?,
    val deviceCategory: DeviceCategoryResponseWithoutChildren?,
)
