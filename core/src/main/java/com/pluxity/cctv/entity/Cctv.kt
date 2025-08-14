package com.pluxity.cctv.entity

import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("cctv")
class Cctv(
    id: String,
    category: DeviceCategory?,
    // DB 컬럼은 name 유지
    @Column(name = "name")
    var cctvName: String = "",
    @Column(length = 1000)
    var url: String?,
) : Device(id, category) {
    fun updateCctv(request: CctvUpdateRequest) {
        this.cctvName = request.name
        this.url = request.url
    }

    override fun getName(): String {
        return this.cctvName
    }
}
