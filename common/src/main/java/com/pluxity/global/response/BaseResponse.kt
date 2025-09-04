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
        createdBy = this.createdBy!!,
        updatedAt = this.updatedAt.toString(),
        updatedBy = this.updatedBy!!,
    )
