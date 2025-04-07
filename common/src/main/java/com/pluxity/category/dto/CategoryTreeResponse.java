package com.pluxity.category.dto;

import com.pluxity.category.entity.Category;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        List<CategoryTreeResponse> children
) {
    public static <T extends Category<T>> CategoryTreeResponse from(T category) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getChildren().stream()
                        .map(CategoryTreeResponse::from)
                        .toList()
        );
    }
}