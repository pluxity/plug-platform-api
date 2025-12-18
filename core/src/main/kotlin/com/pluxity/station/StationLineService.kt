package com.pluxity.station

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

@Service
class StationLineService(
    private val stationLineRepository: StationLineRepository,
) {
    @Transactional
    fun save(
        station: Station,
        line: Line,
    ) {
        val stationLine = stationLineRepository.save(StationLine(station = station, line = line))
        line.addStationLine(stationLine)
    }

    @Transactional(readOnly = true)
    fun findLinesByStation(station: Station): List<Long> =
        stationLineRepository.findByStationOrderByCreatedAtDesc(station).map { it.line.requiredId }

    @Transactional
    fun deleteByStation(station: Station) {
        stationLineRepository.deleteByStation(station)
    }

    @Transactional(readOnly = true)
    fun findLineMapByStationIds(stations: List<Station>): Map<Station, List<Long>> {
        if (CollectionUtils.isEmpty(stations)) {
            return mapOf()
        }
        return stationLineRepository
            .findByStationInOrderByCreatedAtDesc(stations)
            .groupBy { it.station }
            .mapValues { (_, lines) -> lines.map { it.line.requiredId } }
    }

    @Transactional(readOnly = true)
    fun checkAlreadyConnect(
        station: Station,
        line: Line,
    ): Boolean = stationLineRepository.existsByStationAndLine(station, line)

    @Transactional
    fun deleteStationLine(
        station: Station,
        line: Line,
    ) {
        val stationLine: StationLine =
            stationLineRepository
                .findByStationAndLine(station, line)
                ?: throw CustomException(ErrorCode.NOT_FOUND_STATION_LINE, station.id, line.id)
        line.removeStationLine(stationLine)
        stationLineRepository.delete(stationLine)
    }
}
