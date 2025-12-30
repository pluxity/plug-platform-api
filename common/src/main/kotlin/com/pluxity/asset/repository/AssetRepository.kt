package com.pluxity.asset.repository

import com.pluxity.asset.entity.Asset
import com.pluxity.asset.entity.AssetCategory
import org.springframework.data.jpa.repository.JpaRepository

interface AssetRepository : JpaRepository<Asset, Long> {
    fun findByCategory(category: AssetCategory): List<Asset>

    fun findByCode(code: String): Asset?

    fun findByName(name: String): Asset?

    fun findByNameAndIdNot(
        name: String,
        id: Long,
    ): Asset?

    fun findByCodeAndIdNot(
        code: String,
        id: Long,
    ): Asset?
}
