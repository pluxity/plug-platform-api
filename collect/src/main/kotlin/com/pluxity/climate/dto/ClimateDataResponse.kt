package com.pluxity.climate.dto

import com.pluxity.climate.ClimateData

data class ClimateDataResponse(
    val deviceId: String,
    val uploadTime: String,
    val createdAt: String,
    val temperature: Double,
    val humidity: Double,
    val connStatus: Int,
    val firmwareVersion: String,
    val battery: Double,
)

fun ClimateData.toClimateDataResponse() =
    ClimateDataResponse(
        deviceId = this.deviceId!!,
        uploadTime = this.uploadTime!!.toString(),
        createdAt = this.createdAt.toString(),
        temperature = this.temperature!!,
        humidity = this.humidity!!,
        connStatus = this.status!!,
        firmwareVersion = this.firmwareVersion!!,
        battery = this.battery!!,
    )
