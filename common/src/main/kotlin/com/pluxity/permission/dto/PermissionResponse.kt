package com.pluxity.permission.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.global.response.BaseResponse
import com.pluxity.global.response.toBaseResponse
import com.pluxity.permission.Permission
import com.pluxity.permission.PermissionLevel
import io.swagger.v3.oas.annotations.media.Schema

data class ResourcePermissionResponse(
    val resourceType: String,
    val resourceId: String,
    val level: PermissionLevel,
)

data class DomainPermissionResponse(
    val resourceType: String,
    val level: PermissionLevel,
)

data class PermissionResponse(
    @field:Schema(description = "권한 ID")
    val id: Long,
    @field:Schema(description = "권한 이름")
    val name: String,
    @field:Schema(description = "권한 설명")
    val description: String?,
    @field:Schema(description = "리소스 권한 목록")
    val resourcePermissions: List<ResourcePermissionResponse>,
    @field:Schema(description = "도메인 권한 목록")
    val domainPermissions: List<DomainPermissionResponse>,
    @field:JsonUnwrapped
    val baseResponse: BaseResponse?,
)

fun Permission.toPermissionResponse(): PermissionResponse =
    PermissionResponse(
        id = this.requiredId,
        name = this.name,
        description = this.description,
        resourcePermissions =
            this.resourcePermissions
                .map { permission ->
                    ResourcePermissionResponse(
                        resourceType = permission.resourceName,
                        resourceId = permission.resourceId,
                        level = permission.level,
                    )
                }.sortedWith(compareBy({ it.resourceType }, { it.resourceId })),
        domainPermissions =
            this.domainPermissions
                .map { permission ->
                    DomainPermissionResponse(
                        resourceType = permission.resourceName,
                        level = permission.level,
                    )
                }.sortedBy { it.resourceType },
        baseResponse = this.toBaseResponse(),
    )
