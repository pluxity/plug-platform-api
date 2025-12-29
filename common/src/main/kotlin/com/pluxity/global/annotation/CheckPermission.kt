package com.pluxity.global.annotation

import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.PermissionAction
import com.pluxity.user.entity.PermissionCheckType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckPermission(
    val action: PermissionAction = PermissionAction.READ,
    val phase: PermissionCheckType = PermissionCheckType.SINGLE_ITEM,
    val resourceType: ResourceType = ResourceType.NONE,
    val level: PermissionLevel = PermissionLevel.READ,
    val idParamIndex: Int = 0,
)
