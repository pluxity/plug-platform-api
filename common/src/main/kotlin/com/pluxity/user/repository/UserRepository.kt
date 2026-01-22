package com.pluxity.user.repository

import com.pluxity.user.entity.User
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    @Query(
        """
        SELECT u.username FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        WHERE r.name = :roleName
        """,
    )
    fun findUsernamesByRoleName(roleName: String): List<String>

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
