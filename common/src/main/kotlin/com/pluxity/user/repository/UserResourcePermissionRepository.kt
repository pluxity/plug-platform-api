package com.pluxity.user.repository

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.UserResourcePermission
import org.springframework.data.jpa.repository.JpaRepository

interface UserResourcePermissionRepository : JpaRepository<UserResourcePermission, Long> {
    fun existsByUserIdAndResourceTypeAndResourceId(
        userId: Long,
        resourceType: ResourceType,
        resourceId: String,
    ): Boolean

    fun deleteByUserIdAndResourceTypeAndResourceId(
        userId: Long,
        resourceType: ResourceType,
        resourceId: String,
    )
}
