package com.pluxity.domains.device.dto;

public record NfluxCategoryCreateRequest(
        String name, String contextPath, Long iconFileId, Long parentId) {
    public static NfluxCategoryCreateRequest of(
            String name, String contextPath, Long iconFileId, Long parentId) {
        return new NfluxCategoryCreateRequest(name, contextPath, iconFileId, parentId);
    }
}
