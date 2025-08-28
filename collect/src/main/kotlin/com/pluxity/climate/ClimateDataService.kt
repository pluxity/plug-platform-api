package com.pluxity.climate

import com.pluxity.climate.dto.ClimateDataResponse
import com.pluxity.climate.dto.toClimateDataResponse
import com.pluxity.climate.repository.ClimateDataRepository
import com.pluxity.common.dto.DeviceDataResponse
import com.pluxity.common.dto.toDeviceDataResponse
import com.pluxity.common.enum.DataInterval
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class ClimateDataService(
    private val climateDataRepository: ClimateDataRepository,
) {
    companion object {
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }

    fun getPeriodData(
        id: String,
        interval: DataInterval,
        startIme: String,
        endTime: String,
    ): List<ClimateDataResponse> =
        climateDataRepository
            .findByDeviceIdAndCreatedAtBetween(
                id,
                LocalDateTime.parse(startIme, FORMATTER),
                LocalDateTime.parse(endTime, FORMATTER),
            ).map {
                it.toClimateDataResponse()
            }

    fun getLatestData(id: String): DeviceDataResponse {
        val climateData = climateDataRepository.findTopByDeviceIdOrderByCreatedAtDesc(id) ?: throw CustomException(ErrorCode.NOT_FOUND_DATA)
        return climateData.toDeviceDataResponse(id)
    }
}
