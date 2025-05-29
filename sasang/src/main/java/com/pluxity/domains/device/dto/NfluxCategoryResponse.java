package com.pluxity.domains.device.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;

public record NfluxCategoryResponse(
        Long id,
        String name,
        String contextPath,
        FileResponse iconFile,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static NfluxCategoryResponse from(NfluxCategory category) {
        // FileService를 통해 FileResponse 가져오기
        FileResponse iconFileResponse = category.getIconFileId() != null ? FileResponse.empty() : null;

        return new NfluxCategoryResponse(
                category.getId(),
                category.getName(),
                category.getContextPath(),
                iconFileResponse,
                BaseResponse.of(category));
    }
}
