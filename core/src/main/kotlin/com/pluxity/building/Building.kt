package com.pluxity.building

import com.pluxity.facility.Facility
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Entity
@Table(name = "building")
@DiscriminatorValue("BUILDING")
@ConditionalOnProperty(name = ["facility.building.enabled"], havingValue = "true")
class Building(
    name: String,
    description: String?,
) : Facility(name, description)
