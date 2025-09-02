package com.pluxity.feature.repository

import com.pluxity.facility.Facility
import com.pluxity.feature.entity.Feature
import org.springframework.data.jpa.repository.JpaRepository

interface FeatureRepository : JpaRepository<Feature, String> {
    fun findByFacilityOrderByCreatedAtDesc(facility: Facility): List<Feature>

    fun findByAssetId(assetId: Long): List<Feature>
}
