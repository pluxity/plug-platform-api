package com.pluxity.category.extensions

import com.pluxity.category.dto.CategoryResponse
import com.pluxity.category.entity.Category
import com.pluxity.global.response.toBaseResponse

fun <T : Category<T>> Category<T>.toResponse(
    includeChildren: Boolean = false,
    recursive: Boolean = false,
): CategoryResponse {
    val childResponse =
        if (includeChildren) {
            this.children.map {
                it.toResponse(includeChildren = recursive, recursive = recursive)
            }
        } else {
            emptyList()
        }

    return CategoryResponse(
        id = this.id!!,
        name = this.name,
        parentId = this.parent?.id,
        children = childResponse,
        baseResponse = this.toBaseResponse(),
    )
}
