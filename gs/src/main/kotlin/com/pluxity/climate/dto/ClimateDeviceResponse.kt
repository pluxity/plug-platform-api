package com.pluxity.climate.dto

import com.pluxity.climate.ClimateDevice
import com.pluxity.device.dto.DeviceCategoryResponseWithoutChildren
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.file.dto.FileResponse

data class ClimateDeviceResponse(
    val id: String,
    val name: String,
    val feature: FeatureResponse?,
    val deviceCategory: DeviceCategoryResponseWithoutChildren?,
)

fun ClimateDevice.toClimateResponse(thumbnailFile: FileResponse?): ClimateDeviceResponse =
    ClimateDeviceResponse(
        id = this.id,
        name = this.name,
        feature = this.feature?.let { FeatureResponse.from(it) },
        deviceCategory = this.category?.let { DeviceCategoryResponseWithoutChildren.from(it, thumbnailFile) },
    )
