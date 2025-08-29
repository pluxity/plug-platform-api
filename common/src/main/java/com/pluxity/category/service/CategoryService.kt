package com.pluxity.category.service

import com.pluxity.category.dto.CategoryResponse
import com.pluxity.category.dto.CategoryTreeResponse
import com.pluxity.category.entity.Category
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import org.springframework.data.jpa.repository.JpaRepository
import java.util.function.Function
import java.util.function.Supplier

abstract class CategoryService<T : Category<T?>?> {
    protected abstract val repository: JpaRepository<T?, Long?>?

    fun create(category: T?, parent: T?): Long? {
        category!!.assignToParent(parent)
        return this.repository!!.save<T?>(category).getId()
    }

    fun update(id: Long, name: String?, parentId: Long?) {
        val categoryToUpdate = findById(id)
        val newParent = MappingUtils.findByIdIfExists<Long?, T?>(parentId, Function { id: Long? -> this.findById(id!!) })

        if (categoryToUpdate!!.getId() == parentId) {
            throw CustomException(ErrorCode.INVALID_PARENT_CATEGORY)
        }

        if (isCircularReference(categoryToUpdate, newParent)) {
            throw CustomException(ErrorCode.CIRCULAR_REFERENCE_CATEGORY)
        }

        categoryToUpdate.updateName(name)
        categoryToUpdate.assignToParent(newParent)
    }

    private fun isCircularReference(source: T?, target: T?): Boolean {
        var target = target
        while (target != null) {
            if (target.getId() == source!!.getId()) {
                return true
            }
            target = target.getParent()
        }
        return false
    }

    fun findById(id: Long): T? {
        return this.repository!!
            .findById(id)
            .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_CATEGORY, id) })
    }

    val rootCategories: MutableList<T?>
        get() = this.repository!!.findAll().stream().filter { obj: T? -> obj!!.isRoot() }.toList()

    fun getChildren(parentId: Long): MutableList<T?>? {
        val parent = findById(parentId)
        return parent!!.getChildren()
    }

    val rootCategoryResponses: MutableList<CategoryResponse?>
        get() = this.rootCategories.stream().map<CategoryResponse?> { category: T? -> CategoryResponse.Companion.from(category) }.toList()

    fun getChildResponses(parentId: Long): MutableList<CategoryResponse?> {
        return getChildren(parentId)!!.stream().map<CategoryResponse?> { category: T? -> CategoryResponse.Companion.from(category) }.toList()
    }

    fun getResponse(id: Long): CategoryResponse {
        return CategoryResponse.Companion.from<T?>(findById(id))
    }

    val categoryTree: MutableList<CategoryTreeResponse?>
        get() = this.rootCategories.stream().map<CategoryTreeResponse?> { category: T? -> CategoryTreeResponse.Companion.from(category) }.toList()
}
