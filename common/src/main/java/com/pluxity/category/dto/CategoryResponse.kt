package com.pluxity.category.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.category.entity.Category
import com.pluxity.global.response.BaseResponse

data class CategoryResponse(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val children: List<CategoryResponse>,
    @JsonUnwrapped val baseResponse: BaseResponse,
) {
    companion object {
        fun <T : Category<T>> from(category: T): CategoryResponse =
            CategoryResponse(
                id = category.id!!,
                name = category.name,
                parentId = category.parent?.id,
                children = category.children.map { from(it) },
                baseResponse = BaseResponse.of(category),
            )
    }
}
