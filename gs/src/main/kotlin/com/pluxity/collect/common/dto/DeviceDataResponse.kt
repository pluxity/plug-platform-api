package com.pluxity.collect.common.dto

data class DeviceDataResponse(
    val meta: MetaData,
    val timestamp: String,
    val metrics: Map<String, MetricData>,
)

data class MetricData(
    val unit: String,
    val value: Double,
)

data class MetaData(
    val deviceId: String,
    val query: QueryInfo,
)

data class QueryInfo(
    val metrics: List<String>,
)

data class DeviceListDataResponse(
    val meta: ListMetaData,
    val timestamps: List<String>,
    val metrics: Map<String, ListMetricData>,
)

data class ListMetaData(
    val deviceId: String,
    val query: ListQueryInfo,
)

data class ListQueryInfo(
    val timeUnit: String,
    val from: String,
    val to: String,
    val metrics: List<String>,
)

data class ListMetricData(
    val unit: String,
    val values: List<Double>,
)
