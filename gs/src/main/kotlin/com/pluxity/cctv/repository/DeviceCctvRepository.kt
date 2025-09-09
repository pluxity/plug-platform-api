package com.pluxity.cctv.repository

import com.pluxity.cctv.entity.DeviceCctv
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DeviceCctvRepository : JpaRepository<DeviceCctv, Long> {
    @Modifying
    @Query("delete from DeviceCctv d where d.cctv.id in :ids")
    fun deleteByCctvIdIn(ids: List<String>)
}
