package com.pluxity.user.repository

import com.pluxity.permission.PermissionGroup
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RolePermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RolePermissionRepository : JpaRepository<RolePermission, Long> {
    fun deleteAllByPermissionGroup(permissionGroup: PermissionGroup)

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role = :role")
    fun deleteAllByRole(
        @Param("role") role: Role,
    )
}
