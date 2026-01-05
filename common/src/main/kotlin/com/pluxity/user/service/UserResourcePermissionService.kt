package com.pluxity.user.service

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.UserResourcePermission
import com.pluxity.user.repository.UserResourcePermissionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
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

    @Transactional
    fun create(
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

    @Transactional
    fun delete(
        resourceType: ResourceType,
        resourceId: String,
    ) {
        userResourcePermissionRepository.deleteByResourceTypeAndResourceId(
            resourceType,
            resourceId,
        )
    }
}
