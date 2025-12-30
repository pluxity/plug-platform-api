package com.pluxity.collect.climate

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class ClimateData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var deviceId: String,
    var temperature: Double,
    var humidity: Double,
    var status: Int,
    var firmwareVersion: String,
    var battery: Double,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var uploadTime: LocalDateTime,
)
