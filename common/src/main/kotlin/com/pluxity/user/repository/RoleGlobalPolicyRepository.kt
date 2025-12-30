package com.pluxity.user.repository

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.RoleGlobalPermissionType
import com.pluxity.user.entity.RoleGlobalPolicy
import org.springframework.data.jpa.repository.JpaRepository

interface RoleGlobalPolicyRepository : JpaRepository<RoleGlobalPolicy, Long> {
    fun existsByRoleIdInAndResourceType(
        roleIds: Collection<Long>,
        resourceType: ResourceType,
    ): Boolean

    fun existsByRoleIdInAndResourceTypeAndPermissionTypeIn(
        roleIds: Collection<Long>,
        resourceType: ResourceType,
        permissionTypes: Collection<RoleGlobalPermissionType>,
    ): Boolean

    fun findAllByRoleId(roleId: Long): List<RoleGlobalPolicy>
}
