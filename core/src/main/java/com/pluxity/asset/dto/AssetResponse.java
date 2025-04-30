package com.pluxity.asset.dto;

import com.pluxity.asset.entity.Asset;
import com.pluxity.file.dto.FileResponse;

import java.time.LocalDateTime;

public record AssetResponse(
        Long id,
        String name,
        String type,
        FileResponse file,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AssetResponse from(Asset asset, FileResponse file) {
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getType(),
                file != null ? file : FileResponse.empty(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }
}
