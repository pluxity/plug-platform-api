package com.pluxity.onboarding.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.onboarding.dto.OnboardingLineResponse
import com.pluxity.onboarding.dto.OnboardingStationResponse
import com.pluxity.station.LineRepository
import com.pluxity.station.StationLine
import com.pluxity.station.StationLineRepository
import com.pluxity.station.StationRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingLineService(
    private val lineRepository: LineRepository,
    private val stationLineRepository: StationLineRepository,
    private val stationRepository: StationRepository,
) {
    // station에 line 추가
    @Transactional
    fun putStationLine(
        stationId: Long,
        lineIds: List<Long>,
    ): Long {
        val foundStation =
            stationRepository.findByIdOrNull(stationId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_STATION, stationId)

        val foundLines = lineRepository.findAllById(lineIds)
        if (foundLines.size != lineIds.size) {
            val foundLineIds = foundLines.mapNotNull { it.id }.toSet()
            val notFoundIds = lineIds.filterNot { foundLineIds.contains(it) }
            throw CustomException(ErrorCode.NOT_FOUND_LINE, notFoundIds.joinToString())
        }

        if (stationLineRepository.existsByStationAndLineIn(foundStation, foundLines)) {
            throw CustomException(ErrorCode.ALREADY_CONNECTED_LINE, foundStation.id)
        }

        foundLines.forEach { line ->
            val stationLine =
                StationLine(
                    station = foundStation,
                    line = line,
                )
            stationLineRepository.save(stationLine)
            line.addStationLine(stationLine)
        }

        return stationId
    }

    // station에 line 삭제
    @Transactional
    fun deleteStationLine(
        stationId: Long,
        lineId: Long,
    ) {
        val foundStation =
            stationRepository.findByIdOrNull(stationId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_STATION, stationId)

        val foundLine =
            lineRepository.findByIdOrNull(lineId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_LINE, lineId)

        val deletedCount = stationLineRepository.deleteByStationAndLine(foundStation, foundLine)
        if (deletedCount == 0) {
            throw CustomException(ErrorCode.NOT_FOUND_STATION_LINE)
        }
    }

    // 환승역 조회
    @Transactional(readOnly = true)
    fun findStationTwoLine(): List<OnboardingStationResponse> {
        val stations = stationLineRepository.findStationIdsWithMultipleLines()
        if (stations.isEmpty()) {
            return emptyList()
        }

        val stationLines = stationLineRepository.findByStationInWithLines(stations)

        return stationLines
            .groupBy { it.station }
            .map { (station, lines) ->
                OnboardingStationResponse(
                    name = station.name,
                    lines = lines.map { OnboardingLineResponse(it.line.name!!) },
                )
            }
    }
}
