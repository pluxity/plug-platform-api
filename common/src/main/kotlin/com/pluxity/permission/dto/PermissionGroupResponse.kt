package com.pluxity.permission.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.global.response.BaseResponse
import com.pluxity.global.response.toBaseResponse
import com.pluxity.permission.PermissionGroup
import io.swagger.v3.oas.annotations.media.Schema

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
        id = this.requiredId,
        name = this.name,
        description = this.description,
        permissions =
            this.permissions.groupBy { it.resourceName }.map { (resourceType, permissions) ->
                PermissionResponse(
                    resourceType = resourceType,
                    permissions =
                        permissions.map { permission ->
                            PermissionItemResponse(
                                resourceId = permission.resourceId,
                                level = permission.level,
                            )
                        },
                )
            },
        baseResponse = this.toBaseResponse(),
    )
