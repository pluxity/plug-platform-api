package com.pluxity.asset.repository

import com.pluxity.asset.entity.AssetCategory
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.user.entity.ExecutionPhase
import com.pluxity.user.entity.PermissionType
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface AssetCategoryRepository : JpaRepository<AssetCategory, Long> {
    fun existsByCode(code: String): Boolean

    fun findByParentId(parentId: Long): List<AssetCategory>

    @CheckPermission(type = PermissionType.ID, phase = ExecutionPhase.FILTER)
    override fun findAll(sort: Sort): List<AssetCategory>
}
