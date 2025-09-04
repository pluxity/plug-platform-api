package com.pluxity.building

import com.pluxity.facility.Facility
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "building")
@DiscriminatorValue("BUILDING")
class Building(
    name: String,
    description: String?,
) : Facility(name = name, description = description)
