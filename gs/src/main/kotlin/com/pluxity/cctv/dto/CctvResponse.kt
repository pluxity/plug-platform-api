package com.pluxity.cctv.dto

import com.pluxity.cctv.entity.Cctv
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.feature.dto.toFeatureResponse

data class CctvResponse(
    val id: String,
    val name: String,
    val url: String?,
    val feature: FeatureResponse?,
)

fun Cctv.toCctvResponse() =
    CctvResponse(
        id = this.id,
        name = this.name,
        url = this.url,
        feature = this.feature?.toFeatureResponse(),
    )
