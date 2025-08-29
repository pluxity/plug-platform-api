package com.pluxity.climate

import org.springframework.data.jpa.repository.JpaRepository

interface ClimateDataRepository : JpaRepository<ClimateData, Long> {
    fun findTopByDeviceIdOrderByCreatedAtDesc(deviceId: String): ClimateData?
}
