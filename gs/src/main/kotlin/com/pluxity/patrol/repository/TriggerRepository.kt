package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.Trigger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TriggerRepository : JpaRepository<Trigger, Long> {
    @Query(
        """
        SELECT t.* FROM trigger t
        INNER JOIN scenario s ON t.scenario_id = s.id
        WHERE t.is_active = true
          AND s.is_active = true
          AND (t.execute_hour IS NULL OR t.execute_hour = :hour)
          AND (t.execute_minute IS NULL OR t.execute_minute = :minute)
          AND (t.start_date IS NULL OR t.start_date <= :currentDate)
          AND (t.end_date IS NULL OR t.end_date >= :currentDate)
          AND (
            (t.trigger_type = 'ONCE' AND t.month = :month AND t.day_of_month = :dayOfMonth)
            OR
            (t.trigger_type = 'REPEAT' AND (t.day_of_week & :dayOfWeekBit) > 0)
          )
        """,
        nativeQuery = true,
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
