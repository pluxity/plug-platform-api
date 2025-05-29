package com.pluxity.domains.device.dto;

public record NfluxCategoryUpdateRequest(String name, String contextPath, Long iconFileId) {

    public static NfluxCategoryUpdateRequest of(String name, String contextPath, Long iconFileId) {
        return new NfluxCategoryUpdateRequest(name, contextPath, iconFileId);
    }
}
