package com.pluxity.category.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.global.response.BaseResponse

data class CategoryResponse(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val children: List<CategoryResponse>,
    @field:JsonUnwrapped val baseResponse: BaseResponse,
)
