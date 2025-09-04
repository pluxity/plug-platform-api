package com.pluxity.facility

import com.pluxity.global.annotation.CheckPermission
import com.pluxity.user.entity.ExecutionPhase
import com.pluxity.user.entity.PermissionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FacilityRepository : JpaRepository<Facility, Long> {
    override fun findAll(): List<Facility>

    override fun findById(id: Long): Optional<Facility>

    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Optional<Facility>

    fun countByIdIn(ids: List<Long>): Long

    @CheckPermission(type = PermissionType.ID, phase = ExecutionPhase.FILTER)
    fun findAllByOrderByCreatedAtDesc(): List<Facility>
}
