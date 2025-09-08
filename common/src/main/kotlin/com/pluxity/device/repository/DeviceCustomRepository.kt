package com.pluxity.device.repository

import com.pluxity.device.entity.Device

interface DeviceCustomRepository {
    fun findByIdOrNullCustom(id: String): Device?

    fun findAllByFacilityIdIfPresent(facilityId: Long?): List<Device>
}
