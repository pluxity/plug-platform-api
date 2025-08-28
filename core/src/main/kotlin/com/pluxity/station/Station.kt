package com.pluxity.station

import com.pluxity.facility.Facility
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "station")
@DiscriminatorValue("STATION")
class Station(
    name: String,
    description: String? = null,
) : Facility(name, description)
