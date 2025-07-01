package com.pluxity.category.service;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND;

import com.pluxity.category.dto.CategoryResponse;
import com.pluxity.category.dto.CategoryTreeResponse;
import com.pluxity.category.entity.Category;
import com.pluxity.category.entity.CategoryType;
import com.pluxity.category.repository.CategoryRepository;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Long create(String name, CategoryType type, Long parentId) {
        Category parent = parentId != null ? findById(parentId) : null;
        Category category = Category.builder()
                .name(name)
                .type(type)
                .parent(parent)
                .build();
        
        return categoryRepository.save(category).getId();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND));
    }

    public List<Category> findRootCategoriesByType(CategoryType type) {
        return categoryRepository.findByTypeAndParentIsNull(type);
    }

    public List<Category> findChildrenByParentId(Long parentId) {
        Category parent = findById(parentId);
        return parent.getChildren();
    }

    public List<CategoryResponse> getRootCategoryResponsesByType(CategoryType type) {
        return findRootCategoriesByType(type).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryResponse> getChildResponsesByParentId(Long parentId) {
        return findChildrenByParentId(parentId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse getResponse(Long id) {
        return CategoryResponse.from(findById(id));
    }

    public List<CategoryTreeResponse> getCategoryTreeByType(CategoryType type) {
        return findRootCategoriesByType(type).stream()
                .map(CategoryTreeResponse::from)
                .toList();
    }

    @Transactional
    public void updateName(Long id, String newName) {
        Category category = findById(id);
        category.updateName(newName);
    }

    @Transactional
    public void assignToParent(Long categoryId, Long parentId) {
        Category category = findById(categoryId);
        Category parent = parentId != null ? findById(parentId) : null;
        category.assignToParent(parent);
    }
}
