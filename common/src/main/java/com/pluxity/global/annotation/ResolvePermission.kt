package com.pluxity.global.annotation

import com.pluxity.user.entity.PermissionType
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ResolvePermission(
    val value: PermissionType,
)
