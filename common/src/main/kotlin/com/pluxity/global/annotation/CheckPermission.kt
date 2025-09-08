package com.pluxity.global.annotation

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.ExecutionPhase
import com.pluxity.user.entity.PermissionType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckPermission(
    val type: PermissionType,
    val target: String = "#returnObject",
    val phase: ExecutionPhase = ExecutionPhase.AFTER,
    val resourceType: ResourceType = ResourceType.NONE,
)
