package com.pluxity.asset.repository

import com.pluxity.asset.entity.AssetCategory
import com.pluxity.global.annotation.CheckPermissionAll
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface AssetCategoryRepository : JpaRepository<AssetCategory?, Long?> {
    fun findByCode(code: String?): Optional<AssetCategory?>?

    fun existsByCode(code: String?): Boolean

    @Query("SELECT ac FROM AssetCategory ac WHERE ac.parent IS NULL")
    fun findAllRootCategories(): MutableList<AssetCategory?>?

    fun findByParentId(parentId: Long?): MutableList<AssetCategory?>?

    @Query("SELECT ac FROM AssetCategory ac LEFT JOIN FETCH ac.assets WHERE ac.id = :id")
    fun findByIdWithAssets(id: Long?): Optional<AssetCategory?>?

    @CheckPermissionAll(resourceName = "DEVICE_CATEGORY")
    override fun findAll(sort: Sort): MutableList<AssetCategory?>
}
