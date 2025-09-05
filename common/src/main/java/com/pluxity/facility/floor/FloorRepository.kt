package com.pluxity.facility.floor

import com.pluxity.facility.Facility
import org.hibernate.annotations.BatchSize
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FloorRepository : JpaRepository<Floor?, Long?> {
    @BatchSize(size = 2)
    fun findAllByFacility(facility: Facility?): MutableList<Floor?>?

    @Query("SELECT f FROM Floor f WHERE f.facility IN :facilities")
    fun <T : Facility?> findAllByFacilities(facilities: MutableList<T?>?): MutableList<Floor?>?

    @Modifying
    @Query("DELETE FROM Floor f WHERE f.facility = :facility")
    fun deleteByFacility(facility: Facility?)
}
