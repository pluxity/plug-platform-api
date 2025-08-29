package com.pluxity.common.enum

enum class DataInterval(
    val unit: String,
    val description: String,
) {
    HOUR("hour", "시간별"),
    DAY("day", "일별"),
    WEEK("week", "주별"),
    MONTH("month", "월별"),
    YEAR("year", "년별"),
}
