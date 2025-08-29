package com.pluxity.category.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.category.entity.Category
import com.pluxity.global.response.BaseResponse

@JvmRecord
data class CategoryTreeResponse(
    val id: Long?,
    val name: String?,
    val children: MutableList<CategoryTreeResponse?>?,
    @field:JsonUnwrapped @param:JsonUnwrapped val baseResponse: BaseResponse?
) {
    companion object {
        fun <T : Category<T?>?> from(category: T?): CategoryTreeResponse {
            return CategoryTreeResponse(
                category!!.getId(),
                category.getName(),
                category.getChildren().stream().map<CategoryTreeResponse?> { category: T? -> from(category) }.toList(),
                BaseResponse.of(category)
            )
        }
    }
}
