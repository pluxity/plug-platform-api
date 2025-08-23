package com.pluxity.park

import com.pluxity.facility.Facility
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Entity
@Table(name = "park")
@DiscriminatorValue("PARK")
@ConditionalOnProperty(name = ["facility.park.enabled"], havingValue = "true")
class Park(
    name: String,
    description: String?,
    var boundary: String?,
) : Facility(name, description) {
    fun updateBoundary(boundary: String?) {
        this.boundary = boundary
    }
}
