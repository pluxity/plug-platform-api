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
            throw CustomException(ErrorCode.NOT_FOUND_LINE)
        }

        if (stationLineRepository.existsByStationAndLineIn(foundStation, foundLines)) {
            throw CustomException(ErrorCode.ALREADY_CONNECTED_LINE, "이미 연결된 노선있습니다.")
        }

        foundLines.forEach { line ->
            stationLineRepository
                .save(
                    StationLine(
                        station = foundStation,
                        line = line,
                    ),
                ).let {
                    line.addStationLine(it)
                }
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
        val result = mutableListOf<OnboardingStationResponse>()

        val stations = stationLineRepository.findStationIdsWithMultipleLines()
        val stationLines = stationLineRepository.findByStationInWithLines(stations)

        stationLines
            .groupBy { it.station.id }
            .map { (stationId, lines) ->
                val station = lines.first().station
                result.add(
                    OnboardingStationResponse(
                        name = station.name,
                        lines = lines.map { OnboardingLineResponse(it.line.name!!) },
                    ),
                )
            }
        return result
    }
}
