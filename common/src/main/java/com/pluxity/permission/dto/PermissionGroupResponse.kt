package com.pluxity.permission.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.global.response.BaseResponse
import com.pluxity.permission.PermissionGroup
import io.swagger.v3.oas.annotations.media.Schema
import kotlin.collections.component1
import kotlin.collections.component2

data class PermissionGroupResponse(
    @field:Schema(description = "권한 그룹 ID")
    val id: Long,
    @field:Schema(description = "권한 그룹 이름")
    val name: String,
    @field:Schema(description = "권한 그룹 설명")
    val description: String?,
    @field:Schema(description = "포함된 권한 목록 ")
    val permissions: List<PermissionResponse>,
    @field:JsonUnwrapped
    val baseResponse: BaseResponse?,
)

fun PermissionGroup.toPermissionGroupResponse(): PermissionGroupResponse =
    PermissionGroupResponse(
        id = this.id!!,
        name = this.name,
        description = this.description,
        permissions =
            this.permissions.groupBy { it.resourceName }.map { (resourceType, permissions) ->
                PermissionResponse(
                    resourceType = resourceType,
                    resourceIds = permissions.map { it.resourceId },
                )
            },
        baseResponse = BaseResponse.of(this),
    )
