package com.pluxity.label3d

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface Label3DRepository : JpaRepository<Label3D, String> {
    @EntityGraph(attributePaths = ["feature.facility"])
    @Query("SELECT l FROM Label3D l WHERE l.feature.facility.id = :facilityId")
    fun findAllByFacilityId(facilityId: Long): List<Label3D>

    @EntityGraph(attributePaths = ["feature.asset"])
    override fun findAll(): List<Label3D>
}
