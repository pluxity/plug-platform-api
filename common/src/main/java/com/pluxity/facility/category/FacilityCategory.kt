package com.pluxity.facility.category

import com.pluxity.category.entity.Category
import com.pluxity.facility.Facility
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Table(name = "facility_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class FacilityCategory @Builder constructor(name: String?) : Category<FacilityCategory?>() {
    @OneToMany(mappedBy = "category")
    private val facilities: MutableList<Facility?> = ArrayList<Facility?>()

    init {
        if (name != null) {
            this.name = name
        }
    }
}
