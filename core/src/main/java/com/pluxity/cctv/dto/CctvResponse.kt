package com.pluxity.cctv.dto

import com.pluxity.cctv.entity.Cctv
import com.pluxity.device.dto.DeviceCategoryResponseWithoutChildren
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.file.dto.FileResponse

data class CctvResponse(
    val id: String,
    val name: String,
    val url: String?,
    val feature: FeatureResponse?,
    val deviceCategory: DeviceCategoryResponseWithoutChildren?,
)

fun Cctv.toCctvResponse(thumbnailFile: FileResponse?) =
    CctvResponse(
        id = this.id,
        name = this.name,
        url = this.url,
        feature = this.feature?.let(FeatureResponse::from),
        deviceCategory = this.category?.let { DeviceCategoryResponseWithoutChildren.from(it, thumbnailFile) },
    )
