package com.pluxity.device

import com.pluxity.device.dto.DeviceInfoResponse
import com.pluxity.device.dto.GsDeviceInfoResponse
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "gs_device")
@DiscriminatorValue("gs_device")
class GsDevice(
    id: String,
    category: DeviceCategory?,
    // DB 컬럼은 name 유지
    @Column(name = "name")
    var deviceName: String = "",
) : Device(id, category) {
    override fun getName(): String {
        return this.deviceName
    }

    override fun toDeviceInfo(): DeviceInfoResponse = GsDeviceInfoResponse(id, deviceName, deviceType.type, feature?.id)

    fun update(name: String?) {
        if (name != null) {
            this.deviceName = name
        }
    }

    fun putUpdate(name: String) {
        this.deviceName = name
    }
}
