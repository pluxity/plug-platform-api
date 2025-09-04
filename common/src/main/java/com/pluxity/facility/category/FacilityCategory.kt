package com.pluxity.facility.category

import com.pluxity.category.entity.Category
import com.pluxity.facility.Facility
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "facility_category")
class FacilityCategory(
    name: String,
) : Category<FacilityCategory>(name) {
    @OneToMany(mappedBy = "category")
    val facilities: List<Facility> = mutableListOf()
}
