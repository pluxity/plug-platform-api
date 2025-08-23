package com.pluxity.climate.entity

import com.pluxity.climate.ClimateDevice
import com.pluxity.device.entity.DeviceCategory

fun dummyClimateDevice(
    id: String = "device-id",
    name: String = "Test Device",
    category: DeviceCategory? = null,
) = ClimateDevice(
    id,
    category,
    name,
)
