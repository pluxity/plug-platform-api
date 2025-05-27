package com.pluxity.domains.device.dto;

import com.pluxity.device.dto.DeviceCategoryRequest;

public record NfluxCategoryUpdateRequest(
        String name, Long parentId, String contextPath, DeviceCategoryRequest deviceCategoryRequest) {
    // 기본 생성자 - 기존 방식을 위해 유지
    public NfluxCategoryUpdateRequest(String name, Long parentId, String contextPath) {
        this(name, parentId, contextPath, null);
    }
}
