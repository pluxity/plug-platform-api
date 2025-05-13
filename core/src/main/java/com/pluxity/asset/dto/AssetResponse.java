package com.pluxity.asset.dto;

import com.pluxity.asset.entity.Asset;
import com.pluxity.file.dto.FileResponse;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AssetResponse(
        Long id,
        String name,
        FileResponse file,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static AssetResponse from(Asset asset, FileResponse file) {
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                file != null ? file : FileResponse.empty(),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }
}
