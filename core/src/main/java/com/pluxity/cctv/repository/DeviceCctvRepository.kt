package com.pluxity.cctv.repository

import com.pluxity.cctv.entity.DeviceCctv
import com.pluxity.device.entity.Device
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DeviceCctvRepository : JpaRepository<DeviceCctv, Long> {
    @EntityGraph(attributePaths = ["cctv", "cctv.category", "cctv.feature", "cctv.feature.facility"])
    fun findByDevice(device: Device): List<DeviceCctv>

    @Modifying
    @Query("delete from DeviceCctv d where d.cctv.id in :ids")
    fun deleteByCctvIdIn(ids: List<String>)

    fun deleteByDevice(device: Device)
}
