package com.pluxity.category.service;

import com.pluxity.category.dto.CategoryCreateRequest;
import com.pluxity.category.dto.CategoryResponse;
import com.pluxity.category.dto.CategoryUpdateRequest;
import com.pluxity.category.entity.Category;

import java.util.List;

public interface CategoryService<T extends Category<T>> {
    void create(CategoryCreateRequest request);
    CategoryResponse<T> findById(Long id);
    List<CategoryResponse<T>> findAll();
    void update(Long id, CategoryUpdateRequest request);
    void delete(Long id);
    CategoryResponse<T> move(Long id, Long newParentId);
} 