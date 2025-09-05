package com.pluxity.device.entity

import com.pluxity.category.entity.Category
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Permissible
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "device_category")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CATEGORY_TYPE")
@DiscriminatorValue("DEVICE_BASE")
class DeviceCategory(
    var categoryName: String = "",
    @Column(name = "icon_file_id")
    var iconFileId: Long? = null,
) : Category<DeviceCategory>(categoryName),
    Permissible {
    @OneToMany(mappedBy = "category") // Persist ALL 하면 생성할때 id 중복되서 오류 발생 가능
    val devices: MutableList<Device> = mutableListOf()

    fun updateIconFileId(iconFileId: Long?) {
        this.iconFileId = iconFileId
    }

    fun addDevice(device: Device?) {
        device?.let { if (!devices.contains(it)) devices.add(it) }
    }

    fun removeDevice(device: Device?) {
        device?.let { devices.remove(it) }
    }

    fun assignToRootPreservingEntity() {
        this.parent = null
        this.validateDepth()
    }

    override val resourceId: String
        get() = this.id.toString()
    override val resourceType: ResourceType
        get() = ResourceType.DEVICE_CATEGORY
}
