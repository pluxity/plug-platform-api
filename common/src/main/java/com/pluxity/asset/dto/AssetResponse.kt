package com.pluxity.asset.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.asset.entity.Asset
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.response.BaseResponse

@JvmRecord
data class AssetResponse(
    @JvmField val id: Long?,
    @JvmField val name: String?,
    @JvmField val code: String?,
    @JvmField val categoryId: Long?,
    @JvmField val categoryName: String?,
    @JvmField val categoryCode: String?,
    @JvmField val file: FileResponse?,
    @JvmField val thumbnailFile: FileResponse?,
    @field:JsonUnwrapped @param:JsonUnwrapped val baseResponse: BaseResponse?
) {
    companion object {
        @JvmOverloads
        fun from(asset: Asset, file: FileResponse? = null, thumbnailFile: FileResponse? = null): AssetResponse {
            return AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getCode(),
                if (asset.getCategory() != null) asset.getCategory().getId() else null,
                if (asset.getCategory() != null) asset.getCategory().getName() else null,
                if (asset.getCategory() != null) asset.getCategory().code else null,
                if (file != null) file else FileResponse.empty(),
                if (thumbnailFile != null) thumbnailFile else FileResponse.empty(),
                BaseResponse.of(asset)
            )
        }
    }
}
