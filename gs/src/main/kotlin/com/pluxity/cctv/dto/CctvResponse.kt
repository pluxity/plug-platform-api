package com.pluxity.cctv.dto

import com.pluxity.cctv.entity.Cctv
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.feature.dto.toFeatureResponse

data class CctvResponse(
    val id: String,
    val name: String,
    val viewUrl: String? = null,
    val url: String?,
    val feature: FeatureResponse?,
)

fun Cctv.toCctvResponse(viewUrl: String) =
    CctvResponse(
        id = this.id,
        name = this.name,
        url = this.url,
        viewUrl = this.mtxName?.let { "$viewUrl/${this.mtxName}" },
        feature = this.feature?.toFeatureResponse(),
    )
