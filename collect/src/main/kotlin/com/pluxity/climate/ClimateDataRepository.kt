package com.pluxity.climate

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ClimateDataRepository : JpaRepository<ClimateData, Long> {
    fun findByDeviceIdAndCreatedAtBetween(
        deviceId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<ClimateData>
}
