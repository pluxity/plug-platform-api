package com.pluxity.category.service;

import static com.pluxity.global.constant.ErrorCode.INVALID_PARENT_CATEGORY;
import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_CATEGORY;

import com.pluxity.category.dto.CategoryResponse;
import com.pluxity.category.dto.CategoryTreeResponse;
import com.pluxity.category.entity.Category;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class CategoryService<T extends Category<T>> {

    protected abstract JpaRepository<T, Long> getRepository();

    public Long create(T category, T parent) {
        category.assignToParent(parent);
        return getRepository().save(category).getId();
    }

    public void update(Long id, String name, Long parentId) {
        T category = findById(id);
        category.updateName(name);
        T parent = parentId != null ? findById(parentId) : null;
        if (parent != null && parent.getId().equals(category.getId())) {
            throw new CustomException(INVALID_PARENT_CATEGORY);
        }
        category.assignToParent(parent);
    }

    public T findById(Long id) {
        return getRepository()
                .findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_CATEGORY, id));
    }

    public List<T> getRootCategories() {
        return getRepository().findAll().stream().filter(Category::isRoot).toList();
    }

    public List<T> getChildren(Long parentId) {
        T parent = findById(parentId);
        return parent.getChildren();
    }

    public List<CategoryResponse> getRootCategoryResponses() {
        return getRootCategories().stream().map(CategoryResponse::from).toList();
    }

    public List<CategoryResponse> getChildResponses(Long parentId) {
        return getChildren(parentId).stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse getResponse(Long id) {
        return CategoryResponse.from(findById(id));
    }

    public List<CategoryTreeResponse> getCategoryTree() {
        return getRootCategories().stream().map(CategoryTreeResponse::from).toList();
    }
}
