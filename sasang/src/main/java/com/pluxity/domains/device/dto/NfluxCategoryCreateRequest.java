package com.pluxity.domains.device.dto;

import com.pluxity.device.dto.DeviceCategoryRequest;

public record NfluxCategoryCreateRequest(
        String name, Long parentId, String contextPath, DeviceCategoryRequest deviceCategoryRequest) {
    // 기본 생성자 - parentId를 받는 기존 방식을 위해 유지
    public NfluxCategoryCreateRequest(String name, Long parentId, String contextPath) {
        this(name, parentId, contextPath, null);
    }
}
