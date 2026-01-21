package com.pluxity.patrol.repository

import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.entity.ScenarioExecution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ScenarioExecutionRepository : JpaRepository<ScenarioExecution, Long> {
    @Query(
        """
        SELECT se FROM ScenarioExecution se
        JOIN FETCH se.scenario s
        LEFT JOIN FETCH se.sceneExecutions sce
        LEFT JOIN FETCH sce.scene
        WHERE se.id = :id
        """,
    )
    fun findByIdWithDetails(
        @Param("id") id: Long,
    ): ScenarioExecution?

    @Query(
        """
        SELECT se FROM ScenarioExecution se
        JOIN FETCH se.scenario s
        WHERE s.id = :scenarioId
          AND (cast(:status as string ) IS NULL OR se.executionStatus = :status)
          AND (cast(:startDate as localdatetime) IS NULL OR se.startedAt >= :startDate)
          AND (cast(:endDate as localdatetime) IS NULL OR se.startedAt <= :endDate)
        ORDER BY se.startedAt DESC
        """,
    )
    fun findByScenarioIdAndFilters(
        @Param("scenarioId") scenarioId: Long,
        @Param("status") status: ScenarioExecutionStatus?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
    ): List<ScenarioExecution>

    @Query(
        """
        SELECT se FROM ScenarioExecution se
        JOIN FETCH se.scenario s
        WHERE s.facility.id = :facilityId
          AND (cast(:status as string) IS NULL OR se.executionStatus = :status)
          AND (cast(:startDate as localdatetime) IS NULL OR se.startedAt >= :startDate)
          AND (cast(:endDate as localdatetime) IS NULL OR se.startedAt <= :endDate)
        ORDER BY se.startedAt DESC
    """,
    )
    fun findByFacilityIdAndFilters(
        @Param("facilityId") facilityId: Long,
        @Param("status") status: ScenarioExecutionStatus?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
    ): List<ScenarioExecution>
}
