package com.pluxity.temperaturehumidity.entity

import com.pluxity.feature.entity.Feature
import com.pluxity.global.entity.BaseEntity
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Permissible
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "temperature_humidity")
class TemperatureHumidity(
    @Id
    val id: String,
    var name: String,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "feature_id")
    var feature: Feature? = null,
) : BaseEntity(),
    Permissible {
    fun changeFeature(feature: Feature?) {
        this.feature = feature
    }

    fun clearAllRelations() {
        feature?.let { changeFeature(null) }
    }

    fun putUpdate(name: String) {
        this.name = name
    }

    override val resourceId: String
        get() = id

    override val resourceType: ResourceType
        get() = ResourceType.TEMPERATURE_HUMIDITY
}
