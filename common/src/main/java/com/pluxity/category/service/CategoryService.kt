package com.pluxity.category.service

import com.pluxity.category.dto.CategoryResponse
import com.pluxity.category.dto.CategoryTreeResponse
import com.pluxity.category.entity.Category
import com.pluxity.global.constant.ErrorCode.CIRCULAR_REFERENCE_CATEGORY
import com.pluxity.global.constant.ErrorCode.INVALID_PARENT_CATEGORY
import com.pluxity.global.constant.ErrorCode.NOT_FOUND_CATEGORY
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import org.springframework.data.jpa.repository.JpaRepository

abstract class CategoryService<T : Category<T>> {
    protected abstract fun getRepository(): JpaRepository<T, Long>

    fun create(
        category: T,
        parent: T?,
    ): Long {
        category.assignToParent(parent)
        return getRepository().save(category).id!!
    }

    fun update(
        id: Long,
        name: String?,
        parentId: Long?,
    ) {
        val categoryToUpdate = findById(id)
        val newParent = MappingUtils.findByIdIfExists(parentId, ::findById)

        if (categoryToUpdate.id == parentId) {
            throw CustomException(INVALID_PARENT_CATEGORY)
        }

        if (isCircularReference(categoryToUpdate, newParent)) {
            throw CustomException(CIRCULAR_REFERENCE_CATEGORY)
        }

        if (name != null) {
            categoryToUpdate.updateName(name)
        }
        categoryToUpdate.assignToParent(newParent)
    }

    private fun isCircularReference(
        source: T,
        target: T?,
    ): Boolean {
        var current = target
        while (current != null) {
            if (current.id == source.id) {
                return true
            }
            current = current.parent
        }
        return false
    }

    fun findById(id: Long): T = getRepository().findById(id).orElseThrow { CustomException(NOT_FOUND_CATEGORY, id) }

    fun getRootCategories(): List<T> = getRepository().findAll().filter { it.isRoot() }

    fun getChildren(parentId: Long): List<T> = findById(parentId).children

    fun getRootCategoryResponses(): List<CategoryResponse> = getRootCategories().map { CategoryResponse.from(it) }

    fun getChildResponses(parentId: Long): List<CategoryResponse> = getChildren(parentId).map { CategoryResponse.from(it) }

    fun getResponse(id: Long): CategoryResponse = CategoryResponse.from(findById(id))

    fun getCategoryTree(): List<CategoryTreeResponse> = getRootCategories().map { CategoryTreeResponse.from(it) }
}
