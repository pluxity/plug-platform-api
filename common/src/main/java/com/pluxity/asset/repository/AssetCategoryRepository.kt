package com.pluxity.asset.repository

import com.pluxity.asset.entity.AssetCategory
import com.pluxity.global.annotation.CheckPermissionAll
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface AssetCategoryRepository : JpaRepository<AssetCategory, Long> {
    fun existsByCode(code: String): Boolean

    fun findByParentId(parentId: Long): List<AssetCategory>

    @CheckPermissionAll(resourceName = "DEVICE_CATEGORY")
    override fun findAll(sort: Sort): List<AssetCategory>
}
