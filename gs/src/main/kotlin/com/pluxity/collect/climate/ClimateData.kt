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
    var deviceId: String? = null,
    var temperature: Double? = null,
    var humidity: Double? = null,
    var status: Int? = null,
    var firmwareVersion: String? = null,
    var battery: Double? = null,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var uploadTime: LocalDateTime? = null,
)
