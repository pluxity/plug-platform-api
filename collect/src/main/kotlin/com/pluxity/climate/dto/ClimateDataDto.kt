package com.pluxity.climate.dto

import com.pluxity.climate.ClimateData
import com.pluxity.common.dto.DeviceDataResponse
import com.pluxity.common.dto.ListMetricData
import com.pluxity.common.dto.MetaData
import com.pluxity.common.dto.MetricData
import com.pluxity.common.dto.QueryInfo

data class ClimateListDto(
    val bucketStart: String, // date_trunc로 만든 버킷 시작 시각
    val avgTemperature: Double,
    val avgHumidity: Double,
)

// 메트릭 정의를 위한 데이터 클래스
data class MetricDefinition(
    val key: String,
    val unit: String,
)

// 공통 메트릭 정의
object ClimateMetrics {
    val TEMPERATURE = MetricDefinition("temperature", "℃")
    val HUMIDITY = MetricDefinition("humidity", "%")

    val ALL = listOf(TEMPERATURE, HUMIDITY)
}

// 단일 값을 위한 확장 함수
inline fun <T> buildMetricMap(
    source: T,
    definitions: List<MetricDefinition>,
    valueExtractor: T.(MetricDefinition) -> Double?,
): Map<String, MetricData> =
    buildMap {
        definitions.forEach { definition ->
            source.valueExtractor(definition)?.let { value ->
                put(definition.key, MetricData(definition.unit, value))
            }
        }
    }

// 리스트 값을 위한 확장 함수
inline fun <T> List<T>.buildListMetricMap(
    definitions: List<MetricDefinition>,
    valueExtractor: T.(MetricDefinition) -> Double,
): Map<String, ListMetricData> =
    buildMap {
        definitions.forEach { definition ->
            val values = this@buildListMetricMap.map { it.valueExtractor(definition) }
            put(definition.key, ListMetricData(definition.unit, values))
        }
    }

fun ClimateData.toMetricMap(): Map<String, MetricData> =
    buildMetricMap(this, ClimateMetrics.ALL) { definition ->
        when (definition.key) {
            "temperature" -> temperature
            "humidity" -> humidity
            else -> null
        }
    }

fun ClimateData.toDeviceDataResponse(deviceId: String): DeviceDataResponse {
    val metricMap = this.toMetricMap()
    val queryInfo = QueryInfo(metricMap.keys.toList())
    val meta = MetaData(deviceId, queryInfo)

    return DeviceDataResponse(
        meta = meta,
        timestamp = this.createdAt.toString(),
        metrics = metricMap,
    )
}
