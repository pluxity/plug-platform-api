package com.pluxity.facility.history

import org.springframework.data.jpa.repository.JpaRepository

interface FacilityHistoryRepository : JpaRepository<FacilityHistory?, Long?> {
    fun findByFacilityIdOrderByCreatedAtDesc(facilityId: Long?): MutableList<FacilityHistory?>?
}
