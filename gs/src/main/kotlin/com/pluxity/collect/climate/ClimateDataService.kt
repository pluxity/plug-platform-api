package com.pluxity.collect.climate

import com.pluxity.collect.climate.dto.ClimateDataResponse
import com.pluxity.collect.climate.dto.toClimateDataResponse
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ClimateDataService(
    private val climateDataRepository: ClimateDataRepository,
) {
    companion object {
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }

    fun findData(
        id: String,
        startIme: Any,
        endTime: String,
    ): List<ClimateDataResponse> =
        climateDataRepository
            .findByDeviceIdAndCreatedAtBetween(
                id,
                LocalDateTime.parse(startIme.toString(), FORMATTER),
                LocalDateTime.parse(endTime, FORMATTER),
            ).map {
                it.toClimateDataResponse()
            }
}
