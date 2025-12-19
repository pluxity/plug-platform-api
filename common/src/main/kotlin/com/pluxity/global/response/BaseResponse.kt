package com.pluxity.global.response

import com.pluxity.global.entity.BaseEntity

data class BaseResponse(
    val createdAt: String,
    val createdBy: String,
    val updatedAt: String,
    val updatedBy: String,
)

fun BaseEntity.toBaseResponse(): BaseResponse =
    BaseResponse(
        createdAt = this.createdAt.toString(),
        createdBy = requireNotNull(this.createdBy) { "createBy is null (not ready)" },
        updatedAt = this.updatedAt.toString(),
        updatedBy = requireNotNull(this.updatedBy) { "updatedBy is null (not ready)" },
    )
