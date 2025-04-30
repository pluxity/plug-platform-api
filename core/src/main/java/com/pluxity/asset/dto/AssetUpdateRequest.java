package com.pluxity.asset.dto;

public record AssetUpdateRequest(
        String type,
        String name,
        Long fileId
) {
}
