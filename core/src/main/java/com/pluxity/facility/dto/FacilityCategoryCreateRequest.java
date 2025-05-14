package com.pluxity.facility.dto;

import jakarta.validation.constraints.NotBlank;

public record FacilityCategoryCreateRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.") String name, Long parentId) {}
