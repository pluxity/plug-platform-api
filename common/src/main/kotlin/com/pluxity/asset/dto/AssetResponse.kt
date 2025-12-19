package com.pluxity.asset.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.asset.entity.Asset
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.response.BaseResponse
import com.pluxity.global.response.toBaseResponse

data class AssetResponse(
    val id: Long,
    val name: String,
    val code: String,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryCode: String?,
    val file: FileResponse?,
    val thumbnailFile: FileResponse?,
    @field:JsonUnwrapped val baseResponse: BaseResponse,
)

fun Asset.toResponse(
    file: FileResponse? = null,
    thumbnailFile: FileResponse? = null,
): AssetResponse =
    AssetResponse(
        id = this.requiredId,
        name = this.name,
        code = this.code,
        categoryId = this.category?.id,
        categoryName = this.category?.name,
        categoryCode = this.category?.code,
        file = file,
        thumbnailFile = thumbnailFile,
        baseResponse = this.toBaseResponse(),
    )
