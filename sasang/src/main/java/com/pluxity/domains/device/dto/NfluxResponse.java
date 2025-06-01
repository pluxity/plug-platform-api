package com.pluxity.domains.device.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.global.response.BaseResponse;

public record NfluxResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String code,
        String description,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static NfluxResponse from(Nflux device) {
        return new NfluxResponse(
                device.getId(),
                device.getCategory() != null ? device.getCategory().getId() : null,
                device.getCategory() != null ? device.getCategory().getName() : null,
                device.getName(),
                device.getDeviceCode(),
                device.getDescription(),
                BaseResponse.of(device));
    }
}
