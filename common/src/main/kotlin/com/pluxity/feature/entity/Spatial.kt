package com.pluxity.feature.entity

import jakarta.persistence.Embeddable

@Embeddable
data class Spatial(
    var x: Double? = 0.0,
    var y: Double? = 0.0,
    var z: Double? = 0.0,
)
