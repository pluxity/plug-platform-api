package com.pluxity.domains.device.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;

public record NfluxResponse(
        String id,
        Long categoryId,
        String categoryName,
        String name,
        FileResponse thumbnail,
        @JsonUnwrapped BaseResponse baseResponse) {
}
