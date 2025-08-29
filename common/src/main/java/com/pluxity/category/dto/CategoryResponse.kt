package com.pluxity.category.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.category.entity.Category
import com.pluxity.global.response.BaseResponse

@JvmRecord
data class CategoryResponse(
    val id: Long?,
    val name: String?,
    val parentId: Long?,
    val children: MutableList<CategoryResponse?>?,
    @field:JsonUnwrapped @param:JsonUnwrapped val baseResponse: BaseResponse?
) {
    companion object {
        fun <T : Category<T?>?> from(category: T?): CategoryResponse {
            return CategoryResponse(
                category!!.getId(),
                category.getName(),
                if (category.getParent() != null) category.getParent()!!.getId() else null,
                category.getChildren().stream().map<CategoryResponse?> { category: T? -> from(category) }.toList(),
                BaseResponse.of(category)
            )
        }
    }
}
