package com.pluxity.facility.category

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface FacilityCategoryRepository : JpaRepository<FacilityCategory, Long> {
    fun findByNameAndParentId(
        name: String,
        parentId: Long?,
    ): FacilityCategory?

    fun findByParentIsNull(sort: Sort): List<FacilityCategory>
}
