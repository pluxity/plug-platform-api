package com.pluxity.category.dto;

import com.pluxity.category.entity.Category;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryResponse<T extends Category<T>>(
        Long id,
        String name,
        String description,
        Long parentId,
        Integer level,
        String path,
        List<CategoryResponse<T>> children
) {

    public static <T extends Category<T>> CategoryResponse<T> from(T category) {
        return CategoryResponse.<T>builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .level(category.getLevel())
                .path(category.getPath())
                .children(category.getChildren().stream()
                        .map(CategoryResponse::from)
                        .toList())
                .build();
    }
}
