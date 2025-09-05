package com.pluxity.permission

import org.springframework.data.jpa.repository.JpaRepository

interface PermissionGroupRepository : JpaRepository<PermissionGroup, Long> {
    fun existsByName(permissionGroupName: String): Boolean

    fun existsByNameAndIdNot(
        newName: String,
        id: Long,
    ): Boolean
}
