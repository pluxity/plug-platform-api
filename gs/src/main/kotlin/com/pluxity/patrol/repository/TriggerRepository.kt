package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.Trigger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface TriggerRepository : JpaRepository<Trigger, Long> {
    @Query(
        """
      SELECT t FROM Trigger t
      join fetch t.scenario s
      join fetch s.facility
      join fetch t.triggerTargets
      WHERE t.isActive = true
        AND t.nextExecutionTime = :executionTime
      """,
    )
    fun findActiveTriggers(executionTime: LocalDateTime): List<Trigger>
}
