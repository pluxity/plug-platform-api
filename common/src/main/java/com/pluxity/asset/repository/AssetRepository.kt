package com.pluxity.asset.repository

import com.pluxity.asset.entity.Asset
import com.pluxity.asset.entity.AssetCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AssetRepository : JpaRepository<Asset?, Long?> {
    fun findByCategory(category: AssetCategory?): MutableList<Asset?>?

    fun findByCode(code: String?): Optional<Asset?>?

    fun existsByCodeAndIdNot(code: String?, id: Long?): Boolean

    fun findByName(name: String?): Optional<Asset?>?

    fun findByNameAndIdNot(name: String?, id: Long?): Optional<Asset?>?

    fun findByCodeAndIdNot(code: String?, id: Long?): Optional<Asset?>?
}
