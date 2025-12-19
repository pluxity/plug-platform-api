package com.pluxity.device.entity

import com.pluxity.feature.entity.Feature
import com.pluxity.global.entity.BaseEntity
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Permissible
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import kotlin.toString

@Entity
@Table(name = "device")
class Device(
    @Id
    val id: String,
    var name: String,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "feature_id")
    var feature: Feature? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: DeviceCategory? = null,
    @Enumerated(EnumType.STRING)
    var deviceType: DeviceType,
    @Enumerated(EnumType.STRING)
    var companyType: DeviceCompanyType,
) : BaseEntity(),
    Permissible {
    init {
        category?.addDevice(this)
    }

    fun changeFeature(feature: Feature?) {
        this.feature = feature
    }

    fun changeCategory(category: DeviceCategory?) {
        this.category?.removeDevice(this)
        this.category = category
        category?.addDevice(this)
    }

    fun clearAllRelations() {
        feature?.let { changeFeature(null) }
        category?.let { changeCategory(null) }
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

    override val resourceId: String
        get() = this.category?.id.toString()
    override val resourceType: ResourceType
        get() = ResourceType.DEVICE_CATEGORY
}
