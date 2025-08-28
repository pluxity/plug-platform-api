package com.pluxity.device.repository

import com.pluxity.device.entity.DeviceCategory
import com.pluxity.global.annotation.CheckPermissionAll
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceCategoryRepository : JpaRepository<DeviceCategory, Long> {
    fun findByParentId(parentId: Long): List<DeviceCategory>

    @CheckPermissionAll(resourceName = "DEVICE_CATEGORY")
    fun findAllBy(sort: Sort): List<DeviceCategory>
}
