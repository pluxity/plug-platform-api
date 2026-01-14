package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.Scenario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ScenarioRepository : JpaRepository<Scenario, Long> {
    @Query(
        """
            SELECT s FROM Scenario s
            JOIN FETCH s.facility
            LEFT JOIN FETCH s.scenarioScenes ss
            LEFT JOIN FETCH ss.scene
            WHERE s.id = :id
        """,
    )
    fun findByIdWithDetails(id: Long): Scenario?

    fun findByFacilityId(facilityId: Long): List<Scenario>

    fun deleteByIdAndFacilityId(
        id: Long,
        facilityId: Long,
    ): Long
}
