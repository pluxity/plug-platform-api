package com.pluxity.domains.device.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.global.response.BaseResponse;

public record NfluxResponse(
        String id,
        Long categoryId,
        String categoryName,
        String name,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static NfluxResponse from(Nflux device) {
        return new NfluxResponse(
                device.getId(),
                device.getCategory() != null ? device.getCategory().getId() : null,
                device.getCategory() != null ? device.getCategory().getName() : null,
                device.getName(),
                BaseResponse.of(device));
    }
}
