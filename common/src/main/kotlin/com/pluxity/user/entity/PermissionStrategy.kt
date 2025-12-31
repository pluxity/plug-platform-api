package com.pluxity.user.entity

import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
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
                hasGlobalPermission(user, resource.resourceType, requiredLevel) ||
                    user.canAccess(resource.resourceType.name, resource.resourceId, requiredLevel) ||
                    hasOwnerPermission(user, resource)
            }
            else -> false
        }

    private fun hasOwnerPermission(
        user: User,
        resource: Permissible,
    ): Boolean {
        val userId = user.id ?: return false
        val resourceId = resource.resourceId

        return userResourcePermissionService.exists(userId, resource.resourceType, resourceId)
    }

    private fun hasGlobalPermission(
        user: User,
        resourceType: ResourceType,
        requiredLevel: PermissionLevel,
    ): Boolean = user.canAccess(resourceType.name, "ALL", requiredLevel)
}
