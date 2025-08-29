package com.pluxity.climate

import com.pluxity.climate.dto.ClimateListDto
import com.pluxity.climate.dto.ClimateMetrics
import com.pluxity.climate.dto.buildListMetricMap
import com.pluxity.climate.dto.toDeviceDataResponse
import com.pluxity.common.dto.DeviceDataResponse
import com.pluxity.common.dto.DeviceListDataResponse
import com.pluxity.common.dto.ListMetaData
import com.pluxity.common.dto.ListMetricData
import com.pluxity.common.dto.ListQueryInfo
import com.pluxity.common.enum.DataInterval
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Service
@Transactional(readOnly = true)
class ClimateDataService(
    private val climateDataRepository: ClimateDataRepository,
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    companion object {
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }

    fun getPeriodData(
        id: String,
        interval: DataInterval,
        startTime: String,
        endTime: String,
    ): DeviceListDataResponse {
        val timeRange = Pair(startTime, endTime).parseTimeRange()
        val (start, end) = timeRange

        val sql =
            """
            SELECT date_trunc('${interval.unit}', c.created_at) AS bucket_start,
                AVG(c.temperature) AS avg_temperature,
                AVG(c.humidity) AS avg_humidity
            FROM climate_data c
            WHERE c.created_at BETWEEN :start AND :end AND c.device_id = :deviceId
            GROUP BY date_trunc('${interval.unit}', c.created_at)
            ORDER BY date_trunc('${interval.unit}', c.created_at) ASC
            """.trimIndent()

        val params =
            mapOf(
                "start" to start,
                "end" to end,
                "deviceId" to id,
            )

        val result =
            jdbcTemplate.query(
                sql,
                params,
            ) { rs, _ ->
                val bucket = rs.getObject("bucket_start", LocalDateTime::class.java)
                val t = rs.getDouble("avg_temperature").round1Decimal()
                val h = rs.getDouble("avg_humidity").round1Decimal()
                ClimateListDto(bucket.format(DateTimeFormatter.ofPattern(interval.format)), t, h)
            }
        return result.toDeviceListDataResponse(id, interval, timeRange)
    }

    fun getLatestData(id: String): DeviceDataResponse {
        val climateData =
            climateDataRepository.findTopByDeviceIdOrderByCreatedAtDesc(id) ?: throw CustomException(
                ErrorCode.NOT_FOUND_DATA,
            )
        return climateData.toDeviceDataResponse(id)
    }

    private fun Pair<String, String>.parseTimeRange(): Pair<LocalDateTime, LocalDateTime> =
        Pair(
            LocalDateTime.parse(first, FORMATTER),
            LocalDateTime.parse(second, FORMATTER),
        )

    private fun Double.round1Decimal(): Double = (this * 10).roundToInt() / 10.0

    private fun List<ClimateListDto>.toDeviceListDataResponse(
        deviceId: String,
        interval: DataInterval,
        timeRange: Pair<LocalDateTime, LocalDateTime>,
    ): DeviceListDataResponse {
        val bucketList = map { it.bucketStart }
        val metrics = toMetricsMap()
        val metaData =
            ListMetaData(
                deviceId,
                ListQueryInfo(
                    interval.name,
                    timeRange.first.toString(),
                    timeRange.second.toString(),
                    metrics.keys.toList(),
                ),
            )

        return DeviceListDataResponse(metaData, bucketList, metrics)
    }

    private fun List<ClimateListDto>.toMetricsMap(): Map<String, ListMetricData> =
        buildListMetricMap(ClimateMetrics.ALL) { definition ->
            when (definition.key) {
                "temperature" -> avgTemperature
                "humidity" -> avgHumidity
                else -> 0.0
            }
        }
}
