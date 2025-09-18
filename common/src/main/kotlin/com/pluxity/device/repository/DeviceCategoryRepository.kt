package com.pluxity.device.repository

import com.pluxity.device.entity.DeviceCategory
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.user.entity.PermissionCheckType
import com.pluxity.user.entity.PermissionType
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceCategoryRepository : JpaRepository<DeviceCategory, Long> {
    fun findByParentId(parentId: Long): List<DeviceCategory>

    @CheckPermission(type = PermissionType.ID, phase = PermissionCheckType.ITEM_LIST)
    fun findAllBy(sort: Sort): List<DeviceCategory>
}
