package com.pluxity.device.entity

import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.entity.DeviceCctv
import com.pluxity.cctv.entity.dummyCctv
import com.pluxity.device.GsDevice

fun dummyGsDevice(
    id: String = "device-id",
    name: String = "Test Device",
    category: DeviceCategory? = null,
) = GsDevice(
    id,
    category,
    name,
)

fun dummyDeviceCctv(
    cctv: Cctv = dummyCctv(),
    device: GsDevice = dummyGsDevice(),
) = DeviceCctv(
    1L,
    device,
    cctv,
)
