package com.pluxity.user.repository

import com.pluxity.user.entity.Role
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {
    @EntityGraph(
        attributePaths = [
            "userRoles.user", "userRoles.role", "rolePermissions.permissionGroup.permissions", "roleGlobalPolicies",
        ],
    )
    fun findWithInfoById(id: Long): Role?

    @EntityGraph(
        attributePaths = [
            "userRoles.user", "userRoles.role", "rolePermissions.permissionGroup.permissions", "roleGlobalPolicies",
        ],
    )
    fun findByAuthIsNotOrderByCreatedAtDesc(auth: String): List<Role>
}
