package com.pluxity.category.service

import com.pluxity.category.dto.CategoryResponse
import com.pluxity.category.entity.Category
import com.pluxity.category.extensions.toResponse
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.constant.ErrorCode.INVALID_PARENT_CATEGORY
import com.pluxity.global.constant.ErrorCode.NOT_FOUND_CATEGORY
import com.pluxity.global.exception.CustomException
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

abstract class CategoryService<T : Category<T>> {
    protected abstract val repository: JpaRepository<T, Long>

    @Transactional
    open fun create(
        category: T,
        parent: T?,
    ): Long =
        repository
            .save(
                category.apply { assignToParent(parent) },
            ).id ?: throw CustomException(ErrorCode.FAILED_TO_SAVE_ENTITY)

    @Transactional
    open fun update(
        id: Long,
        name: String?,
        parentId: Long?,
    ) {
        val categoryToUpdate = findById(id)
        val newParent = parentId?.let { findById(it) }

        require(categoryToUpdate.id != parentId) {
            throw CustomException(INVALID_PARENT_CATEGORY)
        }

        require(!categoryToUpdate.isCircularReferenceWith(newParent)) {
            throw CustomException(ErrorCode.CIRCULAR_REFERENCE_CATEGORY)
        }

        categoryToUpdate.apply {
            name?.let { updateName(it) }
            assignToParent(newParent)
        }
    }

    private fun T.isCircularReferenceWith(newParent: T?): Boolean {
        var current = newParent
        while (current != null) {
            if (current.id == this.id) return true
            current = current.parent
        }
        return false
    }

    fun findById(id: Long): T = repository.findByIdOrNull(id) ?: throw CustomException(NOT_FOUND_CATEGORY, id)

    fun getRootCategories(): List<T> = repository.findAll().filter { it.isRoot() }

    fun getChildren(parentId: Long): List<T> = findById(parentId).children

    // 자식 없이 단일 카테고리 정보만 원할 때
    fun getResponse(id: Long): CategoryResponse = findById(id).toResponse()

    // 직계 자식만 포함하고 싶을 때
    fun getChildResponses(parentId: Long): List<CategoryResponse> {
        val parentCategory = findById(parentId)
        return parentCategory.children.map { it.toResponse() }
    }

    // 전체 트리 구조를 원할 때 (재귀적으로 모든 자식 포함)
    fun getCategoryTree(): List<CategoryResponse> =
        getRootCategories().map {
            it.toResponse(includeChildren = true, recursive = true)
        }
}
