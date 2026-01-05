package com.pluxity.permission

import org.springframework.data.jpa.repository.JpaRepository

interface PermissionRepository : JpaRepository<Permission, Long> {
    fun existsByName(permissionName: String): Boolean

    fun existsByNameAndIdNot(
        newName: String,
        id: Long,
    ): Boolean
}
