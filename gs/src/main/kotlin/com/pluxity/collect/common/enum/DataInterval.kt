package com.pluxity.collect.common.enum

enum class DataInterval(
    val unit: String,
    val format: String,
    val description: String,
) {
    HOUR("hour", "HH:mm", "시간별"),
    DAY("day", "yyyy-MM-dd", "일별"),
    WEEK("week", "yyyy-ww", "주별"),
    MONTH("month", "yyyy-MM", "월별"),
    YEAR("year", "yyyy", "년별"),
}
