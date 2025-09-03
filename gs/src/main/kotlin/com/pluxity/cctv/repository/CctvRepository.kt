package com.pluxity.cctv.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.pluxity.cctv.entity.Cctv
import com.pluxity.feature.entity.Feature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CctvRepository :
    JpaRepository<Cctv, String>,
    KotlinJdslJpqlExecutor {
    @Modifying
    @Query("UPDATE Cctv c SET c.feature = NULL WHERE c.feature = :feature")
    fun updateFeatureByFeature(feature: Feature)

    fun existsByFeature(feature: Feature): Boolean
}
