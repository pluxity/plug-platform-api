package com.pluxity.user.entity

import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.repository.RoleGlobalPolicyRepository
import com.pluxity.user.service.UserResourcePermissionService
import org.springframework.stereotype.Component

@Component
class PermissionStrategy(
    private val userResourcePermissionService: UserResourcePermissionService,
    private val roleGlobalPolicyRepository: RoleGlobalPolicyRepository,
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
    ): Boolean {
        val roleIds = user.getRoles().mapNotNull { it.id }
        if (roleIds.isEmpty()) {
            return false
        }

        val permissionTypes =
            when (requiredLevel) {
                PermissionLevel.READ ->
                    listOf(
                        RoleGlobalPermissionType.READ_ALL,
                        RoleGlobalPermissionType.WRITE_ALL,
                        RoleGlobalPermissionType.ADMIN,
                    )
                PermissionLevel.WRITE ->
                    listOf(
                        RoleGlobalPermissionType.WRITE_ALL,
                        RoleGlobalPermissionType.ADMIN,
                    )
                PermissionLevel.ADMIN ->
                    listOf(
                        RoleGlobalPermissionType.ADMIN,
                    )
            }

        return roleGlobalPolicyRepository.existsByRoleIdInAndResourceTypeAndPermissionTypeIn(
            roleIds,
            resourceType,
            permissionTypes,
        )
    }
}
