package com.pluxity.user.entity

import com.pluxity.permission.PermissionLevel
import com.pluxity.user.service.UserResourcePermissionService
import org.springframework.stereotype.Component

@Component
class PermissionStrategy(
    private val userResourcePermissionService: UserResourcePermissionService,
) {
    fun check(
        user: User,
        resource: Any,
        requiredLevel: PermissionLevel,
    ): Boolean =
        when (resource) {
            is Permissible -> {
                user.canAccess(resource.resourceType.name, resource.resourceId, requiredLevel) ||
                    hasOwnerPermission(user, resource, requiredLevel)
            }
            else -> false
        }

    private fun hasOwnerPermission(
        user: User,
        resource: Permissible,
        requiredLevel: PermissionLevel,
    ): Boolean {
        if (requiredLevel == PermissionLevel.READ) {
            return false
        }

        val userId = user.id ?: return false
        val resourceId = resource.resourceId

        return userResourcePermissionService.exists(userId, resource.resourceType, resourceId)
    }
}
