package com.pluxity.permission

import org.springframework.data.jpa.repository.JpaRepository

interface PermissionRepository : JpaRepository<Permission, Long> {
    fun findByResourceNameAndResourceId(
        resourceName: String,
        resourceId: String,
    ): Permission?

    fun existsByResourceNameAndResourceId(
        resourceName: String,
        resourceId: String,
    ): Boolean

    fun findByResourceNameAndResourceIdIn(
        resourceName: String,
        resourceIds: List<String>,
    ): List<Permission>
}
