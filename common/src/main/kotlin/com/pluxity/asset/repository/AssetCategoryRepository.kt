package com.pluxity.asset.repository

import com.pluxity.asset.entity.AssetCategory
import org.springframework.data.jpa.repository.JpaRepository

interface AssetCategoryRepository : JpaRepository<AssetCategory, Long> {
    fun existsByCode(code: String): Boolean

    fun findByParentId(parentId: Long): List<AssetCategory>
}
