package com.pluxity.device.dto

import com.pluxity.device.GsDevice
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.file.dto.FileResponse

data class GsDeviceResponse(
    val id: String,
    val name: String,
    val feature: FeatureResponse?,
    val deviceCategory: DeviceCategoryResponseWithoutChildren?,
)

fun GsDevice.toGsDeviceResponse(thumbnailFile: FileResponse?): GsDeviceResponse =
    GsDeviceResponse(
        id = this.id,
        name = this.name,
        feature = this.feature?.let(FeatureResponse::from),
        deviceCategory = this.category?.let { DeviceCategoryResponseWithoutChildren.from(it, thumbnailFile) },
    )
