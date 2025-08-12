package com.pluxity.device.entity

import com.pluxity.device.GsDevice

fun dummyGsDevice(
    id: String = "device-id",
    name: String = "Test Device",
    category: DeviceCategory? = null,
)  = GsDevice(
    id,
    category,
    name
)