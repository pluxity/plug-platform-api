package com.pluxity.domains.device.dto;

public record NfluxCreateRequest(
        Long deviceCategoryId, Long asset, String name, String code, String description) {}
