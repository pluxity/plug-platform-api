package com.pluxity.cctv.entity

import com.pluxity.temperaturehumidity.entity.TemperatureHumidity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class DeviceCctv(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @JoinColumn(name = "temperature_humidity_id")
    @ManyToOne(fetch = FetchType.LAZY)
    var temperatureHumidity: TemperatureHumidity,
    @JoinColumn(name = "cctv_id")
    @ManyToOne(fetch = FetchType.LAZY)
    var cctv: Cctv,
)
