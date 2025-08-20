package com.pluxity.station

import org.springframework.data.jpa.repository.JpaRepository

interface StationCodeRepository : JpaRepository<StationCode, Long> {
    fun findByStationOrderByCreatedAtDesc(station: Station): List<StationCode>

    fun deleteByStation(station: Station)

    fun findByStationInOrderByCreatedAtDesc(stationIds: List<Station>): List<StationCode>
}
