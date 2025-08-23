package com.pluxity.station

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

@Service
class StationCodeService(
    private val stationCodeRepository: StationCodeRepository,
) {
    @Transactional
    fun save(
        station: Station,
        code: String,
    ) {
        stationCodeRepository.save(StationCode(station = station, code = code))
    }

    @Transactional(readOnly = true)
    fun findCodesByStation(station: Station): List<String> =
        stationCodeRepository.findByStationOrderByCreatedAtDesc(station).map { it.code }

    @Transactional
    fun deleteByStation(station: Station) {
        stationCodeRepository.deleteByStation(station)
    }

    @Transactional(readOnly = true)
    fun findCodeMapByStationIds(stations: List<Station>): Map<Station, List<String>> {
        if (CollectionUtils.isEmpty(stations)) {
            return mapOf()
        }
        return stationCodeRepository
            .findByStationInOrderByCreatedAtDesc(stations)
            .groupBy { it.station }
            .mapValues { (_, codes) -> codes.map { it.code } }
    }
}
