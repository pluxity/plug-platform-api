package com.pluxity.onboarding

import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType

data class MockData(
    val id: String,
    val name: String,
    val deviceType: DeviceType,
    val companyType: DeviceCompanyType,
)
