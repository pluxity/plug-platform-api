package com.pluxity.user.repository

import com.pluxity.user.entity.Role
import com.pluxity.user.entity.User
import com.pluxity.user.entity.UserRole
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRoleRepository : JpaRepository<UserRole, Long> {
    fun deleteAllByUser(user: User)

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.role = :role")
    fun deleteAllByRole(
        @Param("role") role: Role,
    )

    @EntityGraph(attributePaths = ["user", "role.rolePermissions"])
    override fun findAll(): List<UserRole>
}
