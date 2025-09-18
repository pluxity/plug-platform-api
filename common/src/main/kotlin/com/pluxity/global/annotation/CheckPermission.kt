package com.pluxity.global.annotation

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.PermissionCheckType
import com.pluxity.user.entity.PermissionType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckPermission(
    val type: PermissionType,
    val phase: PermissionCheckType = PermissionCheckType.SINGLE_ITEM,
    val resourceType: ResourceType = ResourceType.NONE,
)
