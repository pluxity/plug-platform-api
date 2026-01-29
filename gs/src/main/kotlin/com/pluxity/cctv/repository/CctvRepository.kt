package com.pluxity.cctv.repository

import com.pluxity.cctv.entity.Cctv
import com.pluxity.feature.entity.Feature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CctvRepository :
    JpaRepository<Cctv, String>,
    CctvCustomRepository {
    @Modifying
    @Query("UPDATE Cctv c SET c.feature = NULL WHERE c.feature = :feature")
    fun updateFeatureByFeature(feature: Feature)

    fun existsByFeature(feature: Feature): Boolean

    @Query("SELECT c.id FROM Cctv c WHERE c.id IN :ids")
    fun findExistingIds(ids: List<String>): List<String>
}
