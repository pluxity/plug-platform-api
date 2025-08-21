package com.pluxity.climate.dto

import java.util.UUID

fun dummyClimateDeviceCreateRequest(): ClimateDeviceCreateRequest =
    ClimateDeviceCreateRequest(
        id = UUID.randomUUID().toString(),
        name = "Test Device",
        categoryId = null,
    )
