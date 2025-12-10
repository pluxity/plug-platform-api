package com.pluxity.station

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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

    @Query(
        """
        select sl.station
        from StationLine sl
        group by sl.station
        having count(distinct sl.line.id) > 1
    """,
    )
    fun findStationIdsWithMultipleLines(): List<Station>

    @Query(
        """
        select sl
        from StationLine sl
        join fetch sl.station s
        join fetch sl.line l
        where s in :stations
    """,
    )
    fun findByStationInWithLines(stations: List<Station>): List<StationLine>

    fun existsByStationAndLineIn(
        station: Station,
        lines: List<Line>,
    ): Boolean

    fun deleteByStationAndLine(
        station: Station,
        line: Line,
    ): Int

    /**
     *  test용
     */
    fun findByStation(station: Station): List<StationLine>

    fun existsByStationId(stationId: Long): Boolean

    fun existsByStation(station: Station): Boolean

    fun findByStationId(stationId: Long): List<StationLine>

    @Query(
        """
        select sl
        from StationLine sl
        join fetch sl.station s
        where s.id = :stationId
    """,
    )
    fun findByStationIdWithQuery(stationId: Long): List<StationLine>
}
