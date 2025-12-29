package com.pluxity.device.entity

import com.pluxity.feature.entity.Feature
import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "device")
class Device(
    @Id
    val id: String,
    var name: String,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "feature_id")
    var feature: Feature? = null,
    @Enumerated(EnumType.STRING)
    var deviceType: DeviceType,
    @Enumerated(EnumType.STRING)
    var companyType: DeviceCompanyType,
) : BaseEntity() {
    fun changeFeature(feature: Feature?) {
        this.feature = feature
    }

    fun clearAllRelations() {
        feature?.let { changeFeature(null) }
    }

    fun putUpdate(
        name: String,
        deviceType: DeviceType,
        companyType: DeviceCompanyType,
    ) {
        this.name = name
        this.deviceType = deviceType
        this.companyType = companyType
    }
}
