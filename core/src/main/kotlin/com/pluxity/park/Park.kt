package com.pluxity.park

import com.pluxity.facility.Facility
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "park")
@DiscriminatorValue("PARK")
class Park(
    name: String,
    description: String?,
    var boundary: String?,
) : Facility(name = name, description = description) {
    fun updateBoundary(boundary: String?) {
        this.boundary = boundary
    }
}
