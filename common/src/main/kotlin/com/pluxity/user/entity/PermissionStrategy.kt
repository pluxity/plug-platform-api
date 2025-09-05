package com.pluxity.user.entity

interface PermissionStrategy {
    fun check(
        user: User,
        resource: Any,
    ): Boolean
}
