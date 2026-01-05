package com.pluxity.user.repository

import com.pluxity.user.entity.User
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    @EntityGraph(
        attributePaths = [
            "userRoles", "userRoles.role",
        ],
    )
    fun findAllBy(sort: Sort): List<User>

    @EntityGraph(
        attributePaths = [
            "userRoles.user",
            "userRoles.role.rolePermissions.permission.resourcePermissions",
            "userRoles.role.rolePermissions.permission.domainPermissions",
        ],
    )
    fun findWithGraphById(id: Long): User?

    @EntityGraph(
        attributePaths = [
            "userRoles", "userRoles.role",
        ],
    )
    fun findByUsername(username: String): User?
}
