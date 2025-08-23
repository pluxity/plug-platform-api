package com.pluxity.climate

import com.pluxity.climate.dto.ClimateDeviceInfoResponse
import com.pluxity.device.dto.DeviceInfoResponse
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "climate_Device")
@DiscriminatorValue("climate_Device")
class ClimateDevice(
    id: String,
    category: DeviceCategory?,
    // DB 컬럼은 name 유지
    @Column(name = "name")
    var deviceName: String = "",
) : Device(id, category) {
    override fun getName(): String = this.deviceName

    override fun toDeviceInfo(): DeviceInfoResponse = ClimateDeviceInfoResponse(id, deviceName, deviceType.type, feature?.id)

    fun putUpdate(name: String) {
        this.deviceName = name
    }
}
