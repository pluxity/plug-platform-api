package com.pluxity.facility

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.springframework.util.StringUtils

@Embeddable
data class FacilityPosition(
    var lon: Double? = null,
    var lat: Double? = null,
    @Column(columnDefinition = "text")
    var locationMeta: String? = null,
) {
    fun merge(
        lon: Double?,
        lat: Double?,
        locationMeta: String?,
    ) {
        if (lon != null) this.lon = lon
        if (lat != null) this.lat = lat
        if (StringUtils.hasText(locationMeta)) this.locationMeta = locationMeta
    }
}
