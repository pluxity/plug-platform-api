package com.pluxity.device.dto

import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.file.dto.FileResponse

class DeviceResponse(
    val id: String,
    val name: String,
    val deviceType: DeviceType,
    val companyType: DeviceCompanyType,
    val feature: FeatureResponse?,
    val deviceCategory: DeviceCategoryResponseWithoutChildren?,
)

fun Device.toDeviceResponse(thumbnailFile: FileResponse?) =
    DeviceResponse(
        id = this.id,
        name = this.name,
        deviceType = this.deviceType,
        companyType = this.companyType,
        feature = this.feature?.let { FeatureResponse.from(it) },
        deviceCategory = this.category?.toDeviceCategoryResponseWithoutChildren(iconFile = thumbnailFile ?: FileResponse()),
    )
