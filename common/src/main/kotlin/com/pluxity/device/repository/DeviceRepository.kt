package com.pluxity.device.repository

import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.facility.Facility
import com.pluxity.feature.entity.Feature
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DeviceRepository :
    JpaRepository<Device, String>,
    DeviceCustomRepository {
    @Modifying
    @Query("UPDATE Device d SET d.feature = NULL WHERE d.feature = :feature")
    fun revokeByFeature(feature: Feature)

    @Query("SELECT d FROM Device d WHERE d.category = :category AND d.feature.facility = :facility")
    fun findByCategoryAndFacility(
        category: DeviceCategory,
        facility: Facility,
    ): List<Device>

    fun existsByFeature(feature: Feature): Boolean

    fun findByCompanyTypeAndDeviceType(
        companyType: DeviceCompanyType,
        deviceType: DeviceType,
    ): List<Device>

    @EntityGraph(attributePaths = ["category"])
    @Query("SELECT d FROM Device d WHERE d.id = :id")
    fun findByIdWithCategory(id: String): Device?

    @EntityGraph(attributePaths = ["feature", "feature.facility"])
    @Query("SELECT d FROM Device d WHERE d.id in :ids")
    fun findByIdInWithFeatureAndFacility(ids: List<String>): List<Device>?
}
