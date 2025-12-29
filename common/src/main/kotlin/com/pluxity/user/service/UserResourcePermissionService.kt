package com.pluxity.user.service

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.UserResourcePermission
import com.pluxity.user.repository.UserResourcePermissionRepository
import org.springframework.stereotype.Service

@Service
class UserResourcePermissionService(
    private val userResourcePermissionRepository: UserResourcePermissionRepository,
) {
    fun exists(
        userId: Long,
        resourceType: ResourceType,
        resourceId: String,
    ): Boolean =
        userResourcePermissionRepository.existsByUserIdAndResourceTypeAndResourceId(
            userId,
            resourceType,
            resourceId,
        )

    fun register(
        userId: Long,
        resourceType: ResourceType,
        resourceId: String,
    ) {
        if (exists(userId, resourceType, resourceId)) {
            return
        }

        userResourcePermissionRepository.save(
            UserResourcePermission(
                userId = userId,
                resourceType = resourceType,
                resourceId = resourceId,
            ),
        )
    }

    fun revoke(
        userId: Long,
        resourceType: ResourceType,
        resourceId: String,
    ) {
        userResourcePermissionRepository.deleteByUserIdAndResourceTypeAndResourceId(
            userId,
            resourceType,
            resourceId,
        )
    }
}
