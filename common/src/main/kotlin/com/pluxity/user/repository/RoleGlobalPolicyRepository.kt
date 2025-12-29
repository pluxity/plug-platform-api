package com.pluxity.user.repository

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.RoleGlobalPolicy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleGlobalPolicyRepository : JpaRepository<RoleGlobalPolicy, Long> {
    fun existsByRoleIdInAndResourceType(
        roleIds: Collection<Long>,
        resourceType: ResourceType,
    ): Boolean

    fun findAllByRoleId(roleId: Long): List<RoleGlobalPolicy>
}
