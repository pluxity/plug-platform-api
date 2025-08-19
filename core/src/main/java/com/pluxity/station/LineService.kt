package com.pluxity.station

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.SortUtils
import com.pluxity.station.dto.LineCreateRequest
import com.pluxity.station.dto.LineResponse
import com.pluxity.station.dto.LineUpdateRequest
import com.pluxity.station.dto.toLineResponse
import lombok.RequiredArgsConstructor
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@RequiredArgsConstructor
class LineService(
    private val lineRepository: LineRepository,
    private val stationLineService: StationLineService,
) {
    @Transactional
    fun save(request: LineCreateRequest): Long {
        lineRepository
            .findByName(request.name)?.let {
                throw CustomException(ErrorCode.DUPLICATE_LINE_NAME, request.name)
            }
        val line =
            Line(
                name = request.name,
                color = request.color,
            )
        return lineRepository.save(line).id!!
    }

    @Transactional(readOnly = true)
    fun findAll(): List<LineResponse> = lineRepository.findAll(SortUtils.getOrderByCreatedAtDesc()).map { it.toLineResponse() }

    @Transactional(readOnly = true)
    fun findById(id: Long): LineResponse = (lineRepository.findByIdOrNull(id) ?: throw notFoundException(id)).toLineResponse()

    @Transactional(readOnly = true)
    fun findLineById(id: Long): Line = lineRepository.findByIdOrNull(id) ?: throw notFoundException(id)

    @Transactional(readOnly = true)
    fun findStationsByLineId(lineId: Long): List<Long> = findLineById(lineId).getStations().map { it.id }

    @Transactional
    fun update(
        id: Long,
        request: LineUpdateRequest,
    ) {
        val line = findLineById(id)

        if (request.name != null) {
            lineRepository
                .findByNameAndIdNot(request.name, id)
                ?.let {
                    throw CustomException(ErrorCode.DUPLICATE_LINE_NAME, request.name)
                }
        }

        line.update(request.name, request.color)
    }

    @Transactional
    fun delete(id: Long) {
        val line = findLineById(id)

        val stations: List<Station> = line.getStations()
        for (station in stations) {
            stationLineService.deleteStationLine(station, line)
        }

        lineRepository.delete(line)
    }

    private fun notFoundException(id: Long): CustomException {
        return CustomException(ErrorCode.NOT_FOUND_LINE, id)
    }
}
