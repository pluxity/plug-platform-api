package com.pluxity.device.dto

import java.util.UUID

fun dummyCreateGsDeviceRequest(): GsDeviceCreateRequest =
    GsDeviceCreateRequest(
        id = UUID.randomUUID().toString(),
        name = "Test Device",
        categoryId = null,
    )
