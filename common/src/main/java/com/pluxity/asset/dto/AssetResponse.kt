package com.pluxity.asset.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.asset.entity.Asset
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.response.BaseResponse

data class AssetResponse(
    val id: Long?,
    val name: String?,
    val code: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryCode: String?,
    val file: FileResponse?,
    val thumbnailFile: FileResponse?,
    @field:JsonUnwrapped val baseResponse: BaseResponse,
) {
    companion object {
        @JvmStatic
        fun from(
            asset: Asset,
            file: FileResponse?,
            thumbnailFile: FileResponse?,
        ): AssetResponse =
            AssetResponse(
                id = asset.id,
                name = asset.name,
                code = asset.code,
                categoryId = asset.category?.id,
                categoryName = asset.category?.name,
                categoryCode = asset.category?.code,
                file = file ?: FileResponse(),
                thumbnailFile = thumbnailFile ?: FileResponse(),
                baseResponse = BaseResponse.of(asset),
            )

        @JvmStatic
        fun from(asset: Asset): AssetResponse = from(asset, null, null)
    }
}
