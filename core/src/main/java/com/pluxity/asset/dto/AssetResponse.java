package com.pluxity.asset.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.asset.entity.Asset;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;

public record AssetResponse(
        Long id,
        String name,
        FileResponse file,
        FileResponse thumbnailFile,
        @JsonUnwrapped BaseResponse baseResponse) {

    public static AssetResponse from(Asset asset, FileResponse file, FileResponse thumbnailFile) {
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                file != null ? file : FileResponse.empty(),
                thumbnailFile != null ? thumbnailFile : FileResponse.empty(),
                BaseResponse.of(asset));
    }

    public static AssetResponse from(Asset asset) {
        return from(asset, null, null);
    }
}
