package com.pluxity.facility

import com.pluxity.global.annotation.CheckPermission
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.PermissionAction
import org.springframework.data.jpa.repository.JpaRepository

interface FacilityRepository : JpaRepository<Facility, Long> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Facility?

    @CheckPermission(action = PermissionAction.READ_LIST, resourceType = ResourceType.FACILITY)
    fun findAllByOrderByCreatedAtDesc(): List<Facility>
}
