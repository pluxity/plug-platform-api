package com.pluxity.device.dto

import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull

class DeviceCreateRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "디바이스 아이디")
    @field:NotNull
    val id: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "디바이스 명")
    val name: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "디바이스 회사")
    val companyType: DeviceCompanyType,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "디바이스 타입")
    val deviceType: DeviceType,
)
