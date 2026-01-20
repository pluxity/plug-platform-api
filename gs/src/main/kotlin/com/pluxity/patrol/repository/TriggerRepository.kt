package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.Trigger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TriggerRepository : JpaRepository<Trigger, Long> {
    @Query(
        """
        SELECT t FROM Trigger t
        JOIN FETCH t.scenario s
        WHERE t.isActive = true
          AND s.isActive = true
          AND t.executeHour = :hour
          AND t.executeMinute = :minute
          AND (t.startDate IS NULL OR t.startDate <= :currentDate)
          AND (t.endDate IS NULL OR t.endDate >= :currentDate)
          AND (
            (t.triggerType = com.pluxity.patrol.constant.TriggerType.ONCE
             AND t.month = :month AND t.dayOfMonth = :dayOfMonth)
            OR
            (t.triggerType = com.pluxity.patrol.constant.TriggerType.REPEAT
             AND MOD(t.dayOfWeek / :dayOfWeekBit, 2) = 1)
          )
        """,
    )
    fun findActiveTriggers(
        @Param("month") month: Int,
        @Param("dayOfMonth") dayOfMonth: Int,
        @Param("hour") hour: Int,
        @Param("minute") minute: Int,
        @Param("dayOfWeekBit") dayOfWeekBit: Int,
        @Param("currentDate") currentDate: LocalDate,
    ): List<Trigger>
}
