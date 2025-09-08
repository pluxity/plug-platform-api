package com.pluxity.user.entity

import com.pluxity.global.annotation.ResolvePermission

@ResolvePermission(PermissionType.TOTAL)
class ResourceAllPermissionStrategy : PermissionStrategy {
    override fun check(
        user: User,
        resource: Any,
    ): Boolean =
        (resource as? Permissible)?.let { permissible ->
            user.canAccess(permissible.resourceType.name, "ALL")
        } ?: false
}
