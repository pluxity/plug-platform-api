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
    ): Boolean {
        return when (resource) {
            is Permissible -> {
                val resourceName = resource.resourceType.name
                if (user.canAccessDomain(resourceName, requiredLevel)) {
                    return true
                }
                user.canAccess(resourceName, resource.resourceId, requiredLevel) ||
                    hasOwnerPermission(user, resource)
            }

            else -> false
        }
    }

    private fun hasOwnerPermission(
        user: User,
        resource: Permissible,
    ): Boolean {
        val userId = user.id ?: return false
        val resourceId = resource.resourceId
        return userResourcePermissionService.exists(userId, resource.resourceType, resourceId)
    }
}
