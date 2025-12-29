package com.pluxity.device.dto

import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType

class DeviceUpdateRequest(
    val name: String,
    val deviceType: DeviceType,
    val companyType: DeviceCompanyType,
)
