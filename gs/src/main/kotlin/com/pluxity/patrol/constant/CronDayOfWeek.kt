package com.pluxity.patrol.constant

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import java.time.DayOfWeek

enum class CronDayOfWeek(
    val bit: Int,
    val cronValue: Int,
    val displayName: String,
) {
    SUN(1, 0, "일"),
    MON(2, 1, "월"),
    TUE(4, 2, "화"),
    WED(8, 3, "수"),
    THU(16, 4, "목"),
    FRI(32, 5, "금"),
    SAT(64, 6, "토"),
    ;

    companion object {
        fun fromCronValue(cronValue: Int): CronDayOfWeek =
            entries.find { it.cronValue == cronValue }
                ?: throw CustomException(ErrorCode.INVALID_CRON_DAY_VALUE, cronValue)

        fun fromDayOfWeek(dayOfWeek: DayOfWeek): CronDayOfWeek = fromCronValue(dayOfWeek.value % 7)
    }
}
