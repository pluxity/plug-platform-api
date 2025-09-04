package com.pluxity.facility

import com.pluxity.global.annotation.CheckPermission
import com.pluxity.user.entity.ExecutionPhase
import com.pluxity.user.entity.PermissionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FacilityRepository : JpaRepository<Facility, Long> {
    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Facility?

    @CheckPermission(type = PermissionType.ID, phase = ExecutionPhase.FILTER)
    fun findAllByOrderByCreatedAtDesc(): List<Facility>
}
