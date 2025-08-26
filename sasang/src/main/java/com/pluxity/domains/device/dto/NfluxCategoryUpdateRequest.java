package com.pluxity.domains.device.dto;

public record NfluxCategoryUpdateRequest(
        String name, String contextPath, Long iconFileId, Long parentId) {

    public static NfluxCategoryUpdateRequest of(
            String name, String contextPath, Long iconFileId, Long parentId) {
        return new NfluxCategoryUpdateRequest(name, contextPath, iconFileId, parentId);
    }
}
