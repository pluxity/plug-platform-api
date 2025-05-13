package com.pluxity.asset.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.asset.constant.AssetType;
import com.pluxity.asset.entity.Asset;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record AssetResponse(
        Long id,
        String name,
        AssetType type,
        FileResponse file,
        @JsonUnwrapped
        BaseResponse baseResponse
) {

    public static AssetResponse from(Asset asset, FileResponse file) {
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getType(),
                file != null ? file : FileResponse.empty(),
                BaseResponse.of(asset)
        );
    }
}
