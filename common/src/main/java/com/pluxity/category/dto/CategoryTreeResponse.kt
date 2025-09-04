package com.pluxity.category.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.category.entity.Category
import com.pluxity.global.response.BaseResponse
import com.pluxity.global.response.toBaseResponse

data class CategoryTreeResponse(
    val id: Long,
    val name: String,
    val children: List<CategoryTreeResponse>,
    @field:JsonUnwrapped val baseResponse: BaseResponse,
) {
    companion object {
        fun <T : Category<T>> from(category: T): CategoryTreeResponse =
            CategoryTreeResponse(
                id = category.id!!,
                name = category.name,
                children = category.children.map { from(it) },
                baseResponse = category.toBaseResponse(),
            )
    }
}
