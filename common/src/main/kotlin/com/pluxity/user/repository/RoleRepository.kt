package com.pluxity.user.repository

import com.pluxity.user.entity.Role
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {
    @EntityGraph(
        attributePaths = [
            "userRoles.user",
            "userRoles.role",
            "rolePermissions.permission.resourcePermissions",
            "rolePermissions.permission.domainPermissions",
        ],
    )
    fun findWithInfoById(id: Long): Role?

    @EntityGraph(
        attributePaths = [
            "userRoles.user",
            "userRoles.role",
            "rolePermissions.permission.resourcePermissions",
            "rolePermissions.permission.domainPermissions",
        ],
    )
    fun findAllByOrderByCreatedAtDesc(): List<Role>
}
