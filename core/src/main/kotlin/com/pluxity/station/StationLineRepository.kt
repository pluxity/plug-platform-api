package com.pluxity.station

import org.springframework.data.jpa.repository.JpaRepository

interface StationLineRepository : JpaRepository<StationLine, Long> {
    fun findByStationOrderByCreatedAtDesc(station: Station): List<StationLine>

    fun deleteByStation(station: Station)

    fun findByStationInOrderByCreatedAtDesc(stations: List<Station>): List<StationLine>

    fun existsByStationAndLine(
        station: Station,
        line: Line,
    ): Boolean

    fun findByStationAndLine(
        station: Station,
        line: Line,
    ): StationLine?
}
