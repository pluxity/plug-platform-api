package com.pluxity.domains.device.dto;

import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import java.util.stream.Collectors;

public record NfluxCategoryResponse(
        Long id,
        String name,
        Long parentId,
        String contextPath,
        Long iconFileId,
        List<NfluxCategoryResponse> children,
        BaseResponse baseResponse) {
    public static NfluxCategoryResponse from(NfluxCategory category) {
        return new NfluxCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getContextPath(),
                category.getIconFileId(),
                category.getChildren().stream()
                        .filter(c -> c instanceof NfluxCategory)
                        .map(c -> NfluxCategoryResponse.from((NfluxCategory) c))
                        .collect(Collectors.toList()),
                BaseResponse.of(category));
    }
}
