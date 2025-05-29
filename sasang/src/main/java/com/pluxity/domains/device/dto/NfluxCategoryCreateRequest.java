package com.pluxity.domains.device.dto;

public record NfluxCategoryCreateRequest(String name, String contextPath, Long iconFileId) {

    public static NfluxCategoryCreateRequest of(String name, String contextPath, Long iconFileId) {
        return new NfluxCategoryCreateRequest(name, contextPath, iconFileId);
    }
}
