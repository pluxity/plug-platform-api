package com.pluxity.facility

import com.pluxity.global.annotation.CheckPermission
import com.pluxity.user.entity.PermissionCheckType
import org.springframework.data.jpa.repository.JpaRepository

interface FacilityRepository : JpaRepository<Facility, Long> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Facility?

    @CheckPermission(phase = PermissionCheckType.ITEM_LIST)
    fun findAllByOrderByCreatedAtDesc(): List<Facility>
}
