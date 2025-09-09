package com.pluxity.facility

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class FacilityPosition(
    var lon: Double? = null,
    var lat: Double? = null,
    @Column(columnDefinition = "text")
    var locationMeta: String? = null,
)
