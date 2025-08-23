package com.pluxity.station

import com.pluxity.facility.Facility
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Entity
@Table(name = "station")
@DiscriminatorValue("STATION")
@ConditionalOnProperty(name = ["facility.station.enabled"], havingValue = "true")
class Station(
    name: String,
    description: String? = null,
) : Facility(name, description)
