package com.pluxity.cctv.dto

data class MediaMtxPathListResponse(
    val pageCount: Int,
    val itemCount: Int,
    val items: List<MediaMtxPathResponse>,
)

data class MediaMtxPathResponse(
    val name: String,
    val source: String,
)

data class MediaMtxRequest(
    val source: String,
    val sourceOnDemand: Boolean = true,
)

data class MediaMtxErrorResponse(
    val error: String,
)
