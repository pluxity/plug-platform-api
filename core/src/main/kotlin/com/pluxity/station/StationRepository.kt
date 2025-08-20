package com.pluxity.station

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StationRepository : JpaRepository<Station, Long> {
    fun findByCode(stationCode: String): Station?
}
