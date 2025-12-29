package com.pluxity.device.repository

import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.feature.entity.Feature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DeviceRepository :
    JpaRepository<Device, String>,
    DeviceCustomRepository {
    @Modifying
    @Query("UPDATE Device d SET d.feature = NULL WHERE d.feature = :feature")
    fun revokeByFeature(feature: Feature)

    fun existsByFeature(feature: Feature): Boolean

    fun findByCompanyTypeAndDeviceType(
        companyType: DeviceCompanyType,
        deviceType: DeviceType,
    ): List<Device>
}
