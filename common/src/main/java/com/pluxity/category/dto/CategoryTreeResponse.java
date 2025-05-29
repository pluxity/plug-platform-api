package com.pluxity.category.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.category.entity.Category;
import com.pluxity.global.response.BaseResponse;
import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        List<CategoryTreeResponse> children,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static <T extends Category<T>> CategoryTreeResponse from(T category) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getChildren().stream().map(CategoryTreeResponse::from).toList(),
                BaseResponse.of(category));
    }
}
