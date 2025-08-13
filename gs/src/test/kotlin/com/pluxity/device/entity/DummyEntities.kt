package com.pluxity.device.entity

import cctv.dummyCctv
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.entity.DeviceCctv
import com.pluxity.device.GsDevice

fun dummyGsDevice(
    id: String = "device-id",
    name: String = "Test Device",
    category: DeviceCategory? = null,
) = GsDevice(
    id,
    category,
    name
)

fun dummyDeviceCctv(
    cctv: Cctv = dummyCctv(),
    device: GsDevice = dummyGsDevice()
) = DeviceCctv(
    device,
    cctv
)