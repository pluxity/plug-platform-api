package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.dto.TriggerRequestType
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.Trigger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TriggerService {
    fun createTrigger(
        request: TriggerRequest,
        scenario: Scenario,
    ): Trigger {
        validateRequest(request)

        val entityTriggerType = if (request.triggerType == TriggerRequestType.ONCE) "ONCE" else "REPEAT"

        val dayOfWeekBit =
            when (request.triggerType) {
                TriggerRequestType.DAILY -> 127
                TriggerRequestType.WEEKLY -> request.daysOfWeek!!.sumOf { it.bit }
                TriggerRequestType.ONCE -> 0
            }

        val cronExpr =
            when (request.triggerType) {
                TriggerRequestType.ONCE -> {
                    val date = request.onceDate!!
                    "once:$date %02d:%02d".format(request.hour, request.minute)
                }
                TriggerRequestType.DAILY -> "0 ${request.minute} ${request.hour} * * ?"
                TriggerRequestType.WEEKLY -> "0 ${request.minute} ${request.hour} ? * ${request.daysOfWeek!!.joinToString(
                    ",",
                ) { it.cronValue.toString() }}"
            }

        val (month, dayOfMonth) =
            if (request.triggerType == TriggerRequestType.ONCE) {
                val date = request.onceDate!!
                date.monthValue to date.dayOfMonth
            } else {
                null to null
            }

        return Trigger(
            scenario = scenario,
            triggerType = TriggerType.valueOf(entityTriggerType),
            executeHour = request.hour,
            executeMinute = request.minute,
            dayOfWeek = dayOfWeekBit,
            month = month,
            dayOfMonth = dayOfMonth,
            startDate = request.startDate ?: LocalDate.now(),
            endDate = request.endDate,
            cronExpression = cronExpr,
            isActive = request.isActive,
        )
    }

    private fun validateRequest(request: TriggerRequest) {
        when (request.triggerType) {
            TriggerRequestType.WEEKLY -> {
                if (request.daysOfWeek.isNullOrEmpty()) {
                    throw CustomException(ErrorCode.TRIGGER_WEEKLY_DAYS_REQUIRED)
                }
            }
            TriggerRequestType.ONCE -> {
                if (request.onceDate == null) {
                    throw CustomException(ErrorCode.TRIGGER_ONCE_DATE_REQUIRED)
                }
            }
            TriggerRequestType.DAILY -> {}
        }
    }
}
