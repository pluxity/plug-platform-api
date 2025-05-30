package com.pluxity.domains.device.dto;

public record NfluxUpdateRequest(
        Long deviceCategoryId, Long asset, String name, String code, String description) {}
