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
    private data class TriggerParams(
        val triggerType: TriggerType,
        val dayOfWeekBit: Int,
        val month: Int?,
        val dayOfMonth: Int?,
        val cronExpression: String,
    )

    fun createTrigger(
        request: TriggerRequest,
        scenario: Scenario,
    ): Trigger {
        val params = buildTriggerParams(request)

        return Trigger(
            scenario = scenario,
            triggerType = params.triggerType,
            executeHour = request.hour,
            executeMinute = request.minute,
            dayOfWeek = params.dayOfWeekBit,
            month = params.month,
            dayOfMonth = params.dayOfMonth,
            startDate = request.startDate ?: LocalDate.now(),
            endDate = request.endDate,
            cronExpression = params.cronExpression,
            isActive = request.isActive,
        )
    }

    fun updateTrigger(
        existingTrigger: Trigger,
        request: TriggerRequest,
    ) {
        val params = buildTriggerParams(request)

        existingTrigger.updateTrigger(
            triggerType = params.triggerType,
            hour = request.hour,
            minute = request.minute,
            dayOfWeek = params.dayOfWeekBit,
            dayOfMonth = params.dayOfMonth,
            month = params.month,
            startDate = request.startDate ?: LocalDate.now(),
            endDate = request.endDate,
            cronExpression = params.cronExpression,
            isActive = request.isActive,
        )
    }

    private fun buildTriggerParams(request: TriggerRequest): TriggerParams {
        validateRequest(request)

        val triggerType =
            if (request.triggerType == TriggerRequestType.ONCE) {
                TriggerType.ONCE
            } else {
                TriggerType.REPEAT
            }

        val (month, dayOfMonth) =
            if (request.triggerType == TriggerRequestType.ONCE) {
                val date = request.onceDate!!
                date.monthValue to date.dayOfMonth
            } else {
                null to null
            }

        return TriggerParams(
            triggerType = triggerType,
            dayOfWeekBit = calculateDayOfWeekBit(request),
            month = month,
            dayOfMonth = dayOfMonth,
            cronExpression = generateCronExpression(request),
        )
    }

    private fun calculateDayOfWeekBit(request: TriggerRequest): Int =
        when (request.triggerType) {
            TriggerRequestType.DAILY -> 127
            TriggerRequestType.WEEKLY -> request.daysOfWeek!!.sumOf { it.bit }
            TriggerRequestType.ONCE -> 0
        }

    private fun generateCronExpression(request: TriggerRequest): String =
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
