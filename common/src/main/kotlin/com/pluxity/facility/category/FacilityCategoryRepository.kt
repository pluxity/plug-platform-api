package com.pluxity.facility.category

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FacilityCategoryRepository : JpaRepository<FacilityCategory, Long> {
    fun findByNameAndParentId(
        name: String,
        parentId: Long?,
    ): FacilityCategory?

    fun findByParentIsNull(sort: Sort): List<FacilityCategory>
}
