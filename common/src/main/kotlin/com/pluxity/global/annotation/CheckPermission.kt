package com.pluxity.global.annotation

import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.PermissionAction

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckPermission(
    val action: PermissionAction = PermissionAction.READ_SINGLE,
    val resourceType: ResourceType = ResourceType.NONE,
    val level: PermissionLevel = PermissionLevel.READ,
    val idParamIndex: Int = 0,
)
