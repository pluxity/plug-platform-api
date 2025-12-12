package com.pluxity.user.repository

import com.pluxity.user.entity.User
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    @EntityGraph(
        attributePaths = [
            "userRoles", "userRoles.role",
        ],
    )
    fun findAllBy(sort: Sort): List<User>

    @EntityGraph(
        attributePaths = [
            "userRoles.user", "userRoles.role.rolePermissions.permissionGroup.permissions",
        ],
    )
    fun findWithGraphById(id: Long): User?

    @EntityGraph(
        attributePaths = [
            "userRoles", "userRoles.role",
        ],
    )
    fun findByUsername(username: String): User?

    @Query(
        """
    SELECT DISTINCT u FROM User u
    JOIN u.userRoles ur
    JOIN ur.role r
    JOIN r.rolePermissions rp
    JOIN rp.permissionGroup pg
    JOIN pg.permissions p
    WHERE p.resourceName = :resourceName
      AND p.resourceId = :resourceId
""",
    )
    fun findByPermission(
        @Param("resourceName") resourceName: String,
        @Param("resourceId") resourceId: Long,
    ): List<User>
}
