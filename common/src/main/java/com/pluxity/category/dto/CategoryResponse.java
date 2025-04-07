package com.pluxity.category.dto;

import com.pluxity.category.entity.Category;

import java.util.List;

public record CategoryResponse(
    Long id,
    String name,
    Long parentId,
    List<CategoryResponse> children
) {
    public static <T extends Category<T>> CategoryResponse from(T category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getChildren().stream()
                        .map(CategoryResponse::from)
                        .toList()
        );
    }
}
